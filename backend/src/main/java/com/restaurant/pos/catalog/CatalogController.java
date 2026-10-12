package com.restaurant.pos.catalog;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController
{
    private final CatalogRepository products;
    private final CategoryRepository categories;

    CatalogController(CatalogRepository catalogRepository,
                      CategoryRepository categoryRepository)
    {
        products = catalogRepository;
        categories = categoryRepository;
    }

    @GetMapping
    public List<ProductResponse> list()
    {
        return products
                .findAll()
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    @GetMapping("/available")
    public List<ProductResponse> available()
    {
        return products
                .findByAvailableTrueOrderByName()
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    ProductResponse create(@Valid @RequestBody ProductRequest productRequest)
    {
        var product = save(new Product(), productRequest);
        return ProductResponse.from(product);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ProductResponse update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest productRequest)
    {
        var product = products
                .findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException("Product not found")
                );
        var productSaved = save(product, productRequest);
        return ProductResponse.from(productSaved);
    }

    private Product save(Product product, ProductRequest productRequest)
    {
        product.name = productRequest.name();
        product.description = productRequest.description();
        product.price = productRequest.price();
        product.available = productRequest.available();
        product.category = categories
                .findById(productRequest.categoryId())
                .orElseThrow(
                        () -> new IllegalArgumentException("Category not found"));
        return products.save(product);
    }

    @GetMapping("/categories")
    public List<Category> cats()
    {
        return categories.findAll();
    }
}
