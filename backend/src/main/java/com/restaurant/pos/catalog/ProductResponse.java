package com.restaurant.pos.catalog;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        boolean available,
        String category)
{
    static ProductResponse from(Product product)
    {
        return new ProductResponse(
                product.id,
                product.name,
                product.description,
                product.price,
                product.available,
                product.category.code);
    }
}
