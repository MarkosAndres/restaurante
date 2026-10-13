package com.restaurant.pos.catalog;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogService
{
    private final CatalogRepository catalogRepository;

    CatalogService(CatalogRepository catalogRepository)
    {
        this.catalogRepository = catalogRepository;
    }

    public List<Product> findAll()
    {
        return catalogRepository.findAll();
    }

    public List<Product> findAvailable()
    {
        return catalogRepository.findByAvailableTrueOrderByName();
    }

    public Product findById(Long id)
    {
        return catalogRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }

    public Product save(Product product)
    {
        return catalogRepository.save(product);
    }
}
