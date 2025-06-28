package com.services.productservice.repositories;

import com.services.productservice.models.Category;
import com.services.productservice.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByCategory(Category category, Pageable pageable);

    List<Product> findByBrand(String brand);

    List<Product> findByNameContainingIgnoreCase(String keyword);

    @Override
    @EntityGraph(attributePaths = "category")
    List<Product> findAll();

    @Override
    @EntityGraph(attributePaths = "category")
    Optional<Product> findById(Long id);
}
