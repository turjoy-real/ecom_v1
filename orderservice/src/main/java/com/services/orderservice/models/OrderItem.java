package com.services.orderservice.models;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String productId;
    private String productName;
    private Double price;
    private Integer quantity;
    
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
} 