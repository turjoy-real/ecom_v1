package com.services.orderservice.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Data
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "order")
    private List<OrderItem> items;

    private double totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;


    private LocalDateTime orderDate;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;


    @LastModifiedDate
    private LocalDateTime updatedAt;

    private String addressId;

    @Column(nullable = true)
    private String paymentMethod;

    private String paymentLink;
}
