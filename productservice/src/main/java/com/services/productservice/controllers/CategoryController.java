package com.services.productservice.controllers;

import com.services.productservice.models.Category;
import com.services.productservice.repositories.CategoryRepository;
import com.services.productservice.services.ProductService;
import com.services.productservice.services.CategoryService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryRepository categoryRepo;
    private final ProductService productService;
    private final CategoryService categoryService;

    public CategoryController(CategoryRepository categoryRepo, ProductService productService, CategoryService categoryService) {
        this.categoryRepo = categoryRepo;
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable("id") Long id) {
        return categoryService.getCategoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        return ResponseEntity.ok(categoryService.createCategory(category));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> updateCategory(@PathVariable("id") Long id, @RequestBody Category category) {
        return ResponseEntity.ok(categoryService.updateCategory(id, category));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable("id") Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAllProductsByCategory(@PathVariable("id") Long id) {
        productService.deleteAllProductsByCategoryId(id);
        return ResponseEntity.noContent().build();
    }
}