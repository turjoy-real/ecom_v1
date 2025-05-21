package com.services.productservice.repositories;

import com.services.productservice.models.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.elasticsearch.annotations.Query;

import java.util.List;

public interface ProductElasticsearchRepository extends ElasticsearchRepository<ProductDocument, String> {
    // Basic search with pagination
    Page<ProductDocument> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Search by category with pagination
    Page<ProductDocument> findByCategoryName(String categoryName, Pageable pageable);

    // Search by brand with pagination
    Page<ProductDocument> findByBrand(String brand, Pageable pageable);

    // Fuzzy search with pagination
    @Query("{\"fuzzy\": {\"name\": {\"value\": \"?0\", \"fuzziness\": \"AUTO\"}}}")
    Page<ProductDocument> findByNameFuzzy(String name, Pageable pageable);

    // Price range search with pagination
    Page<ProductDocument> findByPriceBetween(Double minPrice, Double maxPrice, Pageable pageable);

    // Search by multiple criteria with pagination
    Page<ProductDocument> findByCategoryNameAndBrandAndPriceBetween(
            String categoryName,
            String brand,
            Double minPrice,
            Double maxPrice,
            Pageable pageable);

    // Search by stock availability with pagination
    Page<ProductDocument> findByStockQuantityGreaterThan(Integer quantity, Pageable pageable);

    // Full text search across multiple fields with pagination
    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"name^2\", \"description\", \"brand\", \"category.name\"]}}")
    Page<ProductDocument> searchByText(String text, Pageable pageable);
}
