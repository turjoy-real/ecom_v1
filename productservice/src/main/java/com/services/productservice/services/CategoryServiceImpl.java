package com.services.productservice.services;

import com.services.productservice.models.Category;
import com.services.productservice.repositories.CategoryRepository;
import com.services.productservice.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepo;

    public CategoryServiceImpl(CategoryRepository categoryRepo) {
        this.categoryRepo = categoryRepo;
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepo.findAll();
    }

    @Override
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepo.findById(id);
    }

    @Override
    public Category createCategory(Category category) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name must not be empty");
        }
        if (categoryRepo.findByName(category.getName()).isPresent()) {
            throw new IllegalArgumentException("Category with this name already exists");
        }
        return categoryRepo.save(category);
    }

    @Override
    public Category updateCategory(Long id, Category category) {
        return categoryRepo.findById(id)
                .map(existingCategory -> {
                    existingCategory.setName(category.getName());
                    return categoryRepo.save(existingCategory);
                })
                .orElseThrow(() -> new NotFoundException("Category not found"));
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));
        categoryRepo.delete(category);
    }
} 