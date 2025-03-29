package com.services.cartservice.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "cart_items")
public class CartItem {
    @Id
    private String id;
    private String productId;
    private String productName;
    private double price;
    private int quantity;
    private String userId;
}