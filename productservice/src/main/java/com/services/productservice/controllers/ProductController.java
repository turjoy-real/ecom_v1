package com.services.productservice.controllers;

import com.services.common.dtos.ProductResponse;
import com.services.productservice.dtos.ProductRequest;

import com.services.productservice.services.ProductService;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.createProduct(request));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(productService.getAllProducts(pageNumber, pageSize));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@Valid @PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/{id}/verify-stock")
    @Transactional(readOnly = true)
    public ResponseEntity<Boolean> verifyStock(@PathVariable Long id, @RequestParam int quantity) {
        return ResponseEntity.ok(productService.verifyStock(id, quantity));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(productService.searchProductsWithPagination(keyword, pageNumber, pageSize));
    }

    @GetMapping("/search/category")
    public ResponseEntity<Page<ProductResponse>> searchByCategory(
            @RequestParam String categoryName,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(productService.searchByCategory(categoryName, pageNumber, pageSize));
    }

    @GetMapping("/search/brand")
    public ResponseEntity<Page<ProductResponse>> searchByBrand(
            @RequestParam String brand,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(productService.searchByBrand(brand, pageNumber, pageSize));
    }

    @GetMapping("/search/price")
    public ResponseEntity<Page<ProductResponse>> searchByPriceRange(
            @RequestParam Double minPrice,
            @RequestParam Double maxPrice,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(productService.searchByPriceRange(minPrice, maxPrice, pageNumber, pageSize));
    }

    @GetMapping("/search/advanced")
    public ResponseEntity<Page<ProductResponse>> searchByMultipleCriteria(
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(productService.searchByMultipleCriteria(
                categoryName, brand, minPrice, maxPrice, pageNumber, pageSize));
    }

    @GetMapping("/search/stock")
    public ResponseEntity<Page<ProductResponse>> searchByStockAvailability(
            @RequestParam Integer minQuantity,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(productService.searchByStockAvailability(minQuantity, pageNumber, pageSize));
    }

    @GetMapping("/search/full-text")
    public ResponseEntity<Page<ProductResponse>> fullTextSearch(
            @RequestParam String text,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(productService.fullTextSearch(text, pageNumber, pageSize));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Page<ProductResponse>> getProductsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(productService.getProductsByCategory(category, pageNumber, pageSize));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Void> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        productService.updateProduct(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

}