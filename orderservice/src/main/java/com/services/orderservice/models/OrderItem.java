package com.services.orderservice.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productId;
    private String productName;
    private double price;
    private int quantity;
    private double subtotal;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
}