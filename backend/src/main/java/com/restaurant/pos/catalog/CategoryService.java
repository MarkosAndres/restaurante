package com.restaurant.pos.catalog;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService
{
    private final CategoryRepository categoryRepository;

    CategoryService(CategoryRepository categoryRepository)
    {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> findAll()
    {
        return categoryRepository.findAll();
    }

    public Category findById(Long id)
    {
        return categoryRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
    }
}
