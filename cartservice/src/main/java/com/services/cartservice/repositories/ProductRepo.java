package com.services.cartservice.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.services.cartservice.models.Product;

public interface ProductRepo extends MongoRepository<Product, String>{

    
} 