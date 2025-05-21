package com.services.productservice.services;

import com.services.productservice.dtos.ProductRequest;
import com.services.productservice.dtos.ProductResponse;
import com.services.productservice.models.Product;

import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {
    ProductResponse createProduct(ProductRequest request);

    List<ProductResponse> getAllProducts(int pageNumber, int pageSize);

    ProductResponse getProductById(Long id);

    boolean verifyStock(Long id, int quantity);

    List<ProductResponse> searchProducts(String keyword);

    // New search methods with pagination
    Page<ProductResponse> searchProductsWithPagination(String keyword, int pageNumber, int pageSize);

    Page<ProductResponse> searchByCategory(String categoryName, int pageNumber, int pageSize);

    Page<ProductResponse> searchByBrand(String brand, int pageNumber, int pageSize);

    Page<ProductResponse> searchByPriceRange(Double minPrice, Double maxPrice, int pageNumber, int pageSize);

    Page<ProductResponse> searchByMultipleCriteria(
            String categoryName,
            String brand,
            Double minPrice,
            Double maxPrice,
            int pageNumber,
            int pageSize);

    Page<ProductResponse> searchByStockAvailability(Integer minQuantity, int pageNumber, int pageSize);

    Page<ProductResponse> fullTextSearch(String text, int pageNumber, int pageSize);

    Page<ProductResponse> getProductsByCategory(String category, int pageNumber, int pageSize);

    void updateProduct(Long id, ProductRequest request);

    void deleteProduct(Long id);
}