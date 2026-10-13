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
import java.util.List;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController
{
    private final CatalogService catalogService;
    private final CategoryService categoryService;

    CatalogController(CatalogService catalogService,
                      CategoryService categoryService)
    {
        this.catalogService = catalogService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<ProductResponse> list()
    {
        return catalogService
                .findAll()
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    @GetMapping("/available")
    public List<ProductResponse> available()
    {
        return catalogService
                .findAvailable()
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
        var product = catalogService.findById(id);
        var productSaved = save(product, productRequest);
        return ProductResponse.from(productSaved);
    }

    private Product save(Product product, ProductRequest productRequest)
    {
        product.name = productRequest.name();
        product.description = productRequest.description();
        product.price = productRequest.price();
        product.available = productRequest.available();
        product.category = categoryService.findById(productRequest.categoryId());
        return catalogService.save(product);
    }

    @GetMapping("/categories")
    public List<Category> cats()
    {
        return categoryService.findAll();
    }
}
