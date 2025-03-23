package com.services.productservice.controllers;

import com.services.productservice.models.Category;
import com.services.productservice.repositories.CategoryRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CategoryController {
    private CategoryRepository categoryRepo;

    public CategoryController(CategoryRepository categoryRepo) {
        this.categoryRepo = categoryRepo;
    }

    @GetMapping("/categories/{id}")
    public Category categories(@PathVariable("id") Long id) {
        return categoryRepo.findById(id).get();
    }
}

