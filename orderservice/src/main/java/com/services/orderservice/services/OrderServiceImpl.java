package com.services.orderservice.services;

import com.services.orderservice.dtos.OrderItemRequest;
import com.services.orderservice.dtos.OrderRequest;
import com.services.orderservice.dtos.OrderResponse;
import com.services.orderservice.exceptions.OrderNotFoundException;
import com.services.orderservice.exceptions.UserVerificationException;
import com.services.orderservice.models.Order;
import com.services.orderservice.models.OrderItem;
import com.services.orderservice.models.OrderStatus;
import com.services.orderservice.models.PaymentStatus;
import com.services.orderservice.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;
    private final UserServiceClient userServiceClient;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        // Verify user exists
        if (!userServiceClient.verifyUser(request.getUserId())) {
            throw new UserVerificationException("User not found with ID: " + request.getUserId());
        }

        // Create order
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setStatus(OrderStatus.CREATED);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        order.setShippingAddress(request.getShippingAddress());

        // Create and validate order items
        List<OrderItem> orderItems = request.getItems().stream()
                .map(itemRequest -> {
                    // Verify product exists and has enough stock
                    if (!productServiceClient.verifyStock(itemRequest.getProductId(), itemRequest.getQuantity())) {
                        throw new RuntimeException("Insufficient stock for product: " + itemRequest.getProductId());
                    }

                    // Get product details
                    ProductServiceClient.ProductDetails productDetails = productServiceClient
                            .getProductDetails(itemRequest.getProductId());

                    OrderItem orderItem = new OrderItem();
                    orderItem.setProductId(itemRequest.getProductId());
                    orderItem.setProductName(productDetails.getName());
                    orderItem.setPrice(productDetails.getPrice());
                    orderItem.setQuantity(itemRequest.getQuantity());
                    orderItem.setSubtotal(productDetails.getPrice() * itemRequest.getQuantity());
                    orderItem.setOrder(order);
                    return orderItem;
                })
                .collect(Collectors.toList());

        order.setItems(orderItems);
        // Calculate total amount
        double totalAmount = orderItems.stream()
                .mapToDouble(OrderItem::getSubtotal)
                .sum();
        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);
        return convertToOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return convertToOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(String userId, String status, String paymentStatus, String sortBy,
            String sortDirection) {
        List<Order> orders;

        if (status != null && paymentStatus != null) {
            orders = orderRepository.findByUserIdAndStatusAndPaymentStatus(
                    userId,
                    OrderStatus.valueOf(status.toUpperCase()),
                    PaymentStatus.valueOf(paymentStatus.toUpperCase()));
        } else if (status != null) {
            orders = orderRepository.findByUserIdAndStatus(
                    userId,
                    OrderStatus.valueOf(status.toUpperCase()));
        } else if (paymentStatus != null) {
            orders = orderRepository.findByUserIdAndPaymentStatus(
                    userId,
                    PaymentStatus.valueOf(paymentStatus.toUpperCase()));
        } else {
            orders = orderRepository.findByUserId(userId);
        }

        // Sort the orders
        orders.sort((o1, o2) -> {
            int direction = sortDirection.equalsIgnoreCase("desc") ? -1 : 1;
            switch (sortBy.toLowerCase()) {
                case "orderdate":
                    return direction * o1.getOrderDate().compareTo(o2.getOrderDate());
                case "totalamount":
                    return direction * Double.compare(o1.getTotalAmount(), o2.getTotalAmount());
                case "status":
                    return direction * o1.getStatus().name().compareTo(o2.getStatus().name());
                case "paymentstatus":
                    return direction * o1.getPaymentStatus().name().compareTo(o2.getPaymentStatus().name());
                default:
                    return direction * o1.getOrderDate().compareTo(o2.getOrderDate());
            }
        });

        return orders.stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        try {
            OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());
            order.setStatus(newStatus);
            return convertToOrderResponse(orderRepository.save(order));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid order status: " + status);
        }
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new RuntimeException("Cannot cancel a delivered order");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    private OrderResponse convertToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(String.valueOf(order.getId()));
        response.setUserId(order.getUserId());
        response.setStatus(order.getStatus().name());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setOrderDate(order.getOrderDate());
        response.setCreatedAt(order.getCreatedAt());
        response.setShippingAddress(order.getShippingAddress());

        List<OrderResponse.OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> {
                    OrderResponse.OrderItemResponse itemResponse = new OrderResponse.OrderItemResponse();
                    itemResponse.setProductId(item.getProductId());
                    itemResponse.setProductName(item.getProductName());
                    itemResponse.setPrice(item.getPrice());
                    itemResponse.setQuantity(item.getQuantity());
                    itemResponse.setSubtotal(item.getSubtotal());
                    return itemResponse;
                })
                .collect(Collectors.toList());

        response.setItems(itemResponses);
        response.setTotalAmount(order.getTotalAmount());
        return response;
    }
}
