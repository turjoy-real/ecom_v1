package com.services.cartservice.models;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.redis.core.RedisHash;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@RedisHash("carts")
@Document(collection = "carts")
@AllArgsConstructor
@Getter
@Setter
public class Cart {
    @Id
    private String id;
    private String userId;
    private List<CartItem> items;
}
