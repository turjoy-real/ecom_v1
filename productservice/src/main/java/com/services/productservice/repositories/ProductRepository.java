package com.services.productservice.repositories;


import com.services.productservice.models.Category;
import com.services.productservice.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(Category category);
    List<Product> findByBrand(String brand);
    List<Product> findByNameContainingIgnoreCase(String keyword);
}
