package com.services.productservice.repositories;

import com.services.productservice.models.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.elasticsearch.annotations.Query;

import java.util.List;

public interface ProductElasticsearchRepository extends ElasticsearchRepository<ProductDocument, String> {
    List<ProductDocument> findByNameContainingIgnoreCase(String name);
    List<ProductDocument> findByCategory(String category);
    List<ProductDocument> findByBrand(String brand);
    @Query("{\"fuzzy\": {\"name\": {\"value\": \"?0\", \"fuzziness\": \"AUTO\"}}}")
    List<ProductDocument> findByNameFuzzy(String name);
}
