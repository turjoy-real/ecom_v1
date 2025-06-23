package com.services.orderservice.services;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
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
import com.services.orderservice.exceptions.ProductNotAvailableException;
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
        cartClient.clearCart(token); // this will now run asynchronously
    }

    // Implement methods from OrderService interface
    @Override
    @Transactional
    public OrderResponse createOrderFromCart(Authentication authentication, Long addressId) {

        // Implementation logic here
        // 1. Get items from cart
        String token = "Bearer " + authentication.getCredentials();
        log.info("Token: {}", authentication.getCredentials());

        CartResponse cartResponse = cartClient.getCart(token);
        List<CartItemDTO> products = cartResponse.getItems();
        // 2. Check if product quantities match -> throw error to reduce to cancel
        // product from order
        for (CartItemDTO item : products) {
            Long productId = Long.parseLong(item.getProductId());
            int requestedQty = item.getQuantity();

            if (!productClient.verifyStock(productId, requestedQty)) {
                throw new ProductNotAvailableException(productId, requestedQty); // At least one product is not
                                                                                 // available
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
        order.setUserId(authentication.getName());
        order.setPaymentMethod(null);

        order.setStatus(OrderStatus.CREATED);
        order.setPaymentStatus(PaymentStatus.PENDING);

        order.setTotalAmount(cartResponse.getTotal());

        Order savedOrder = orderRepository.save(order);

        // 4. Clear cart
        clearCartAsync(token);

        // 5. Get payment link

        UserDTO userDetails = userClient.getMyProfile(token);
        if (userDetails == null) {
            throw new RuntimeException("User details not found");
        }

        CreatePaymentLinkRequestDto paymentRequest = new CreatePaymentLinkRequestDto();
        paymentRequest.setOrderId(savedOrder.getId().toString());
        paymentRequest.setUserId(authentication.getName());
        paymentRequest.setAmount(savedOrder.getTotalAmount());
        paymentRequest.setCurrency("INR");
        paymentRequest.setCustomerName(userDetails.getName());
        paymentRequest.setCustomerEmail(userDetails.getEmail());
        paymentRequest.setCustomerContact(null);
        paymentRequest.setDescription("Order Payment for Order ID: " + savedOrder.getId());
        paymentRequest.setAcceptPartial(false);
        paymentRequest.setFirstMinPartialAmount(0);
        paymentRequest.setReminderEnable(true);

        String paymentLink = paymentClient.generatePaymentLink(token, paymentRequest);

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
        orderResponse.setUserId(authentication.getName());
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
        orderStatusUpdateProducer.sendOrderStatusUpdate(
                userDetails.getEmail(),
                savedOrder.getId().toString(),
                savedOrder.getStatus().toString());

        return orderResponse; // Placeholder return statement
    }

    @Override
    public boolean updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null)
            return false;
        order.setStatus(OrderStatus.valueOf(status));
        orderRepository.save(order);
        // Send notification
        orderStatusUpdateProducer.sendOrderStatusUpdate(order.getUserId(), order.getId().toString(),
                order.getStatus().toString());
        return true;
    }

    @Override
    public boolean updatePaymentStatus(Long orderId, String paymentStatus) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null)
            return false;
        order.setPaymentStatus(PaymentStatus.valueOf(paymentStatus));
        orderRepository.save(order);
        return true;
    }

}
