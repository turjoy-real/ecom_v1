package com.services.orderservice.services;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.services.common.dtos.CartItemDTO;
import com.services.common.dtos.CartResponse;
import com.services.common.dtos.CreatePaymentLinkRequestDto;
import com.services.common.dtos.UserDTO;
import com.services.orderservice.clients.CartClient;
import com.services.orderservice.clients.PaymentClient;
import com.services.orderservice.clients.ProductClient;
import com.services.orderservice.clients.UserClient;
import com.services.orderservice.dtos.OrderResponse;
import com.services.orderservice.dtos.OrderResponse.OrderItemResponse;
import com.services.orderservice.exceptions.CartServiceException;
import com.services.orderservice.exceptions.InvalidOrderStatusException;
import com.services.orderservice.exceptions.InvalidPaymentStatusException;
import com.services.orderservice.exceptions.OrderNotFoundException;
import com.services.orderservice.exceptions.PaymentServiceException;
import com.services.orderservice.exceptions.ProductNotAvailableException;
import com.services.orderservice.exceptions.UserVerificationException;
import com.services.orderservice.models.Order;
import com.services.orderservice.models.OrderItem;
import com.services.orderservice.models.OrderStatus;
import com.services.orderservice.models.PaymentStatus;
import com.services.orderservice.repositories.OrderRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    // Autowired dependencies
    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final CartClient cartClient;
    private final PaymentClient paymentClient;
    private final UserClient userClient;
    private final OrderStatusUpdateProducer orderStatusUpdateProducer;

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Async
    private void clearCartAsync(String token) {
        try {
            cartClient.clearCart(token);
            log.info("Cart cleared successfully for user");
        } catch (Exception e) {
            log.error("Failed to clear cart asynchronously: {}", e.getMessage(), e);
            // Don't throw exception here as it's async and shouldn't block the main flow
        }
    }

    @Async
    private void updateOrderAsync(Order order) {
        orderRepository.save(order);
    }

    private void checkUserServiceConnectivity() {
        try {
            ResponseEntity<String> healthResponse = userClient.health();
            log.info("User service health check successful: {}", healthResponse.getStatusCode());
        } catch (Exception e) {
            log.error("User service health check failed: {}", e.getMessage(), e);
        }
    }

    // Implement methods from OrderService interface
    @Override
    @Transactional
    public OrderResponse createOrderFromCart(@AuthenticationPrincipal Jwt jwt, Long addressId) {
        if (jwt == null) {
            throw new UserVerificationException("JWT token is required");
        }
        
        if (addressId == null) {
            throw new IllegalArgumentException("Address ID is required");
        }

        // Implementation logic here
        // 1. Get items from cart
        String token = "Bearer " + jwt.getTokenValue();
        String uid = jwt.getSubject();
        log.info("Token: {}", token);

        // Check user service connectivity first
        checkUserServiceConnectivity();

        UserDTO userDetails;
        try {
            log.debug("Attempting to get user profile with token: {}", token.substring(0, Math.min(50, token.length())) + "...");
            ResponseEntity<UserDTO> userResponse = userClient.getMyProfile(token);
            log.debug("User profile response received: {}", userResponse);
            
            if (userResponse == null || userResponse.getBody() == null) {
                log.error("User profile response is null for user: {}", uid);
                throw new UserVerificationException("User profile response is null");
            }
            userDetails = userResponse.getBody();
            log.info("Successfully retrieved user profile for user: {}, email: {}", uid, userDetails.getEmail());
        } catch (Exception e) {
            log.error("Failed to get user profile for user: {}, Error: {}, Token length: {}", uid, e.getMessage(), token.length(), e);
            throw new UserVerificationException("Failed to get user profile: " + e.getMessage(), e);
        }
        

        CartResponse cartResponse;
        try {
            ResponseEntity<CartResponse> cartResponseEntity = cartClient.getCart(token);
            if (cartResponseEntity == null || cartResponseEntity.getBody() == null) {
                log.error("Cart response is null for user: {}", uid);
                throw new CartServiceException("Cart response is null");
            }
            cartResponse = cartResponseEntity.getBody();
            log.info("Successfully retrieved cart for user: {}", uid);
        } catch (Exception e) {
            log.error("Failed to retrieve cart for user: {}, Error: {}", uid, e.getMessage(), e);
            throw new CartServiceException("Failed to retrieve cart: " + e.getMessage(), e);
        }

        if (cartResponse == null || cartResponse.getItems() == null || cartResponse.getItems().isEmpty()) {
            throw new CartServiceException("Cart is empty or invalid");
        }

        List<CartItemDTO> products = cartResponse.getItems();
        // 2. Check if product quantities match -> throw error to reduce to cancel
        // product from order
        for (CartItemDTO item : products) {
            if (item.getProductId() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Invalid cart item: product ID or quantity is invalid");
            }
            
            Long productId = Long.parseLong(item.getProductId());
            int requestedQty = item.getQuantity();

            try {
                if (!productClient.verifyStock(productId, requestedQty)) {
                    throw new ProductNotAvailableException(productId, requestedQty);
                }
            } catch (ProductNotAvailableException e) {
                throw e;
            } catch (Exception e) {
                log.error("Error verifying stock for product: {}", productId, e);
                throw new ProductNotAvailableException("Failed to verify product stock", e);
            }
        }
        // 3. Create order
        Order order = new Order();

        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItemDTO item : products) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(item.getProductId());
            orderItem.setProductName(item.getProductName());
            orderItem.setPrice(item.getPrice() / item.getQuantity());
            orderItem.setSubtotal(item.getPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setOrder(order);

            orderItems.add(orderItem);
        }

        order.setAddressId(String.valueOf(addressId));
        order.setItems(orderItems);
        order.setUserId(uid);
        order.setPaymentMethod(null);
        

        order.setStatus(OrderStatus.CREATED);
        order.setPaymentStatus(PaymentStatus.PENDING);

        order.setTotalAmount(cartResponse.getTotal());

        Order savedOrder;
        try {
            savedOrder = orderRepository.save(order);
        } catch (Exception e) {
            log.error("Failed to save order for user: {}", uid, e);
            throw new RuntimeException("Failed to save order", e);
        }

        // 4. Clear cart
        clearCartAsync(token);

        // 5. Get payment link

    

        CreatePaymentLinkRequestDto paymentRequest = new CreatePaymentLinkRequestDto();
        paymentRequest.setOrderId(savedOrder.getId().toString());
        paymentRequest.setUserId(uid);
        paymentRequest.setAmount(savedOrder.getTotalAmount());
        paymentRequest.setCurrency("INR");
        paymentRequest.setCustomerName(userDetails.getName());
        paymentRequest.setCustomerEmail(userDetails.getEmail());
        paymentRequest.setCustomerContact(null);
        paymentRequest.setDescription("Order Payment for Order ID: " + savedOrder.getId());
        paymentRequest.setAcceptPartial(false);
        paymentRequest.setFirstMinPartialAmount(0);
        paymentRequest.setReminderEnable(true);

        String paymentLink;
        try {
            paymentLink = paymentClient.generatePaymentLink(token, paymentRequest);
        } catch (Exception e) {
            log.error("Failed to generate payment link for order: {}:{}", savedOrder.getId(), e.toString());
            throw new PaymentServiceException("Failed to generate payment link", e);
        }

        savedOrder.setPaymentLink(paymentLink);

        updateOrderAsync(savedOrder);

        // 6. Get shipping address

        // 7. Provide order response

        List<OrderItemResponse> savedItems = new ArrayList<>();

        for (CartItemDTO item : products) {
            OrderItemResponse orderItem = new OrderItemResponse();
            orderItem.setProductId(item.getProductId());
            orderItem.setProductName(item.getProductName());
            orderItem.setPrice(item.getPrice() / item.getQuantity());
            orderItem.setSubtotal(item.getPrice());
            orderItem.setQuantity(item.getQuantity());

            savedItems.add(orderItem);
        }

        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setId(savedOrder.getId().toString());
        orderResponse.setUserId(uid);
        orderResponse.setItems(savedItems);
        orderResponse.setTotalAmount(savedOrder.getTotalAmount());
        orderResponse.setStatus(savedOrder.getStatus());
        orderResponse.setPaymentStatus(savedOrder.getPaymentStatus());
        orderResponse.setOrderDate(savedOrder.getCreatedAt());
        orderResponse.setCreatedAt(savedOrder.getCreatedAt());
        orderResponse.setShippingAddressId(savedOrder.getAddressId());
        orderResponse.setPaymentMethod(savedOrder.getPaymentMethod());
        orderResponse.setPaymentLink(paymentLink);

        // After saving the order and before returning the response, send order status
        // update
        try {
            orderStatusUpdateProducer.sendOrderStatusUpdate(
                    userDetails.getEmail(),
                    savedOrder.getId().toString(),
                    savedOrder.getStatus().toString());
        } catch (Exception e) {
            log.error("Failed to send order status update for order: {}", savedOrder.getId(), e);
            // Don't throw exception here as order creation is successful
        }

        return orderResponse;
    }

    @Override
    public boolean updateOrderStatus(Long orderId, String status) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID is required");
        }
        
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Order status is required");
        }

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            throw new OrderNotFoundException(orderId);
        }
        
        try {
            OrderStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new InvalidOrderStatusException(status);
        }
        
        order.setStatus(OrderStatus.valueOf(status));
        
        try {
            orderRepository.save(order);
        } catch (Exception e) {
            log.error("Failed to update order status for order: {}", orderId, e);
            throw new RuntimeException("Failed to update order status", e);
        }
        
        // Send notification
        try {
            orderStatusUpdateProducer.sendOrderStatusUpdate(order.getUserId(), order.getId().toString(),
                    order.getStatus().toString());
        } catch (Exception e) {
            log.error("Failed to send order status update notification for order: {}", orderId, e);
            // Don't throw exception here as status update is successful
        }
        
        return true;
    }

    @Override
    public boolean updatePaymentStatus(Long orderId, String paymentStatus) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID is required");
        }
        
        if (paymentStatus == null || paymentStatus.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment status is required");
        }

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            throw new OrderNotFoundException(orderId);
        }
        
        try {
            PaymentStatus.valueOf(paymentStatus);
        } catch (IllegalArgumentException e) {
            throw new InvalidPaymentStatusException(paymentStatus);
        }
        
        order.setPaymentStatus(PaymentStatus.valueOf(paymentStatus));
        
        try {
            orderRepository.save(order);
        } catch (Exception e) {
            log.error("Failed to update payment status for order: {}", orderId, e);
            throw new RuntimeException("Failed to update payment status", e);
        }
        
        return true;
    }

}
