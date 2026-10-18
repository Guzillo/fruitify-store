package io.github.guzillo.fruitifystorebackend.product.controller;

import io.github.guzillo.fruitifystorebackend.product.dto.ProductPageResponseDTO;
import io.github.guzillo.fruitifystorebackend.product.dto.ProductCreateRequestDTO;
import io.github.guzillo.fruitifystorebackend.product.dto.ProductResponseDTO;
import io.github.guzillo.fruitifystorebackend.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ProductPageResponseDTO getProducts(
        Pageable pageable) {
        Page<ProductResponseDTO> products = productService.findProducts(pageable);
        return new ProductPageResponseDTO(
            products.getContent(),
            products.getNumber(),
            products.getSize(),
            products.getTotalElements(),
            products.getTotalPages());
    }

    @GetMapping("/{productId}")
    public ProductResponseDTO getProduct(@PathVariable Long productId) {
        return productService.findProduct(productId);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.CREATED)
    public ProductResponseDTO createProduct(@Valid @RequestBody ProductCreateRequestDTO request) {
        return productService.createProduct(request);
    }
}
