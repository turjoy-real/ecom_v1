package com.services.orderservice.services;

import com.services.orderservice.dtos.OrderRequest;
import com.services.orderservice.dtos.OrderResponse;
import com.services.orderservice.dtos.OrderTrackingResponse;
import com.services.orderservice.dtos.ReturnRequestDTO;
import com.services.orderservice.dtos.ReturnRequestResponse;
import com.services.orderservice.exceptions.OrderNotFoundException;
import com.services.orderservice.exceptions.UserVerificationException;
import com.services.orderservice.models.Order;
import com.services.orderservice.models.OrderItem;
import com.services.orderservice.models.OrderStatus;
import com.services.orderservice.models.PaymentStatus;
import com.services.orderservice.models.OrderTracking;
import com.services.orderservice.models.ReturnRequest;
import com.services.orderservice.repositories.OrderRepository;
import com.services.orderservice.repositories.OrderTrackingRepository;
import com.services.orderservice.repositories.ReturnRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderTrackingRepository trackingRepository;
    private final ReturnRequestRepository returnRequestRepository;
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
        order.setAddressId(request.getShippingAddressId());

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

    @Override
    @Transactional(readOnly = true)
    public OrderTrackingResponse getOrderTracking(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        OrderTracking tracking = trackingRepository.findByOrderId(orderId);
        if (tracking == null) {
            throw new RuntimeException("No tracking information found for order: " + orderId);
        }

        return convertToTrackingResponse(tracking);
    }

    @Override
    @Transactional
    public OrderTrackingResponse updateOrderTracking(Long orderId, String trackingNumber, String carrier) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        OrderTracking tracking = trackingRepository.findByOrderId(orderId);
        if (tracking == null) {
            tracking = new OrderTracking();
            tracking.setOrder(order);
        }

        tracking.setTrackingNumber(trackingNumber);
        tracking.setCarrier(carrier);
        tracking.setTrackingUrl(generateTrackingUrl(carrier, trackingNumber));
        tracking.setCurrentStatus("SHIPPED");
        tracking.setEstimatedDeliveryDate(LocalDateTime.now().plusDays(5)); // Example: 5 days delivery estimate

        return convertToTrackingResponse(trackingRepository.save(tracking));
    }

    @Override
    @Transactional
    public ReturnRequestResponse createReturnRequest(Long orderId, ReturnRequestDTO request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Check if return request already exists
        if (returnRequestRepository.findByOrderId(orderId) != null) {
            throw new RuntimeException("Return request already exists for order: " + orderId);
        }

        ReturnRequest returnRequest = new ReturnRequest();
        returnRequest.setOrder(order);
        returnRequest.setReason(request.getReason());
        returnRequest.setDescription(request.getDescription());
        returnRequest.setStatus("PENDING");

        return convertToReturnRequestResponse(returnRequestRepository.save(returnRequest));
    }

    @Override
    @Transactional(readOnly = true)
    public ReturnRequestResponse getReturnRequest(Long returnId) {
        ReturnRequest returnRequest = returnRequestRepository.findById(returnId)
                .orElseThrow(() -> new RuntimeException("Return request not found: " + returnId));
        return convertToReturnRequestResponse(returnRequest);
    }

    @Override
    @Transactional
    public ReturnRequestResponse updateReturnStatus(Long returnId, String status) {
        ReturnRequest returnRequest = returnRequestRepository.findById(returnId)
                .orElseThrow(() -> new RuntimeException("Return request not found: " + returnId));

        returnRequest.setStatus(status);
        if (status.equals("APPROVED")) {
            returnRequest.setProcessedAt(LocalDateTime.now());
            returnRequest.setReturnLabelUrl(generateReturnLabel(returnRequest));
        } else if (status.equals("COMPLETED")) {
            returnRequest.setCompletedAt(LocalDateTime.now());
        }

        return convertToReturnRequestResponse(returnRequestRepository.save(returnRequest));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnRequestResponse> getReturnRequestsByStatus(String status) {
        return returnRequestRepository.findByStatus(status).stream()
                .map(this::convertToReturnRequestResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getOrderAnalytics(String userId) {
        List<Order> orders = orderRepository.findByUserId(userId);

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalOrders", orders.size());
        analytics.put("totalSpent", orders.stream().mapToDouble(Order::getTotalAmount).sum());

        Map<String, Long> statusCounts = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getStatus().name(),
                        Collectors.counting()));
        analytics.put("statusBreakdown", statusCounts);

        Map<String, Long> paymentStatusCounts = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getPaymentStatus().name(),
                        Collectors.counting()));
        analytics.put("paymentStatusBreakdown", paymentStatusCounts);

        return analytics;
    }

    private OrderResponse convertToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(String.valueOf(order.getId()));
        response.setUserId(order.getUserId());
        response.setStatus(order.getStatus().name());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setOrderDate(order.getOrderDate());
        response.setCreatedAt(order.getCreatedAt());
        response.setShippingAddressId(order.getAddressId());

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

    private OrderTrackingResponse convertToTrackingResponse(OrderTracking tracking) {
        OrderTrackingResponse response = new OrderTrackingResponse();
        response.setTrackingNumber(tracking.getTrackingNumber());
        response.setCarrier(tracking.getCarrier());
        response.setTrackingUrl(tracking.getTrackingUrl());
        response.setCurrentStatus(tracking.getCurrentStatus());
        response.setEstimatedDeliveryDate(tracking.getEstimatedDeliveryDate());
        response.setActualDeliveryDate(tracking.getActualDeliveryDate());
        response.setLastUpdated(tracking.getUpdatedAt());
        return response;
    }

    private ReturnRequestResponse convertToReturnRequestResponse(ReturnRequest returnRequest) {
        ReturnRequestResponse response = new ReturnRequestResponse();
        response.setId(returnRequest.getId());
        response.setOrderId(returnRequest.getOrder().getId().toString());
        response.setReason(returnRequest.getReason());
        response.setDescription(returnRequest.getDescription());
        response.setStatus(returnRequest.getStatus());
        response.setReturnLabelUrl(returnRequest.getReturnLabelUrl());
        response.setRequestedAt(returnRequest.getRequestedAt());
        response.setProcessedAt(returnRequest.getProcessedAt());
        response.setCompletedAt(returnRequest.getCompletedAt());
        return response;
    }

    private String generateTrackingUrl(String carrier, String trackingNumber) {
        // In a real implementation, this would generate a carrier-specific tracking URL
        return String.format("https://%s.com/track/%s", carrier.toLowerCase(), trackingNumber);
    }

    private String generateReturnLabel(ReturnRequest returnRequest) {
        // In a real implementation, this would generate a return shipping label
        return String.format("https://returns.example.com/label/%d", returnRequest.getId());
    }
}
