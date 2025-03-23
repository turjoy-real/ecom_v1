package com.services.productservice.services;

import com.services.productservice.dtos.ProductRequest;
import com.services.productservice.dtos.ProductResponse;

import java.util.List;

public interface ProductService {
    ProductResponse createProduct(ProductRequest request);
    List<ProductResponse> getAllProducts(int pageNumber, int pageSize);
    ProductResponse getProductById(Long id);
    List<ProductResponse> searchProducts(String keyword);
    List<ProductResponse> getProductsByCategory(String category);
    void updateProduct(Long id, ProductRequest request);
    void deleteProduct(Long id);
}