package com.restaurant.pos.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CatalogRepository extends JpaRepository<Product, Long>
{
    List<Product> findByAvailableTrueOrderByName();
}

interface CategoryRepository extends JpaRepository<Category, Long>
{
}
