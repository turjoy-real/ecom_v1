package com.services.orderservice.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String orderNumber;
    private String userId;
    private LocalDateTime orderDate;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "order")
    private List<OrderItem> items;
    
    private Double totalAmount;
    private String shippingAddress;
    private String paymentMethod;
    
    // Tracking information
    private String trackingNumber;
    private LocalDateTime estimatedDelivery;
    
    public enum OrderStatus {
        CREATED, 
        PAYMENT_PENDING,
        PAYMENT_VERIFIED,
        PROCESSING,
        SHIPPED,
        DELIVERED,
        CANCELLED
    }
}
