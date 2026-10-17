package io.github.guzillo.fruitifystorebackend.product.controller;

import io.github.guzillo.fruitifystorebackend.product.dto.ProductPageRequestDTO;
import io.github.guzillo.fruitifystorebackend.product.dto.ProductPageResponseDTO;
import io.github.guzillo.fruitifystorebackend.product.dto.ProductResponseDTO;
import io.github.guzillo.fruitifystorebackend.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ModelAttribute;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ProductPageResponseDTO getProducts(
        @Valid @ModelAttribute ProductPageRequestDTO request) {
        Pageable pageable = request.toPageable();
        Page<ProductResponseDTO> products = productService.findProducts(pageable);
        return new ProductPageResponseDTO(
            products.getContent(),
            products.getNumber(),
            products.getSize(),
            products.getTotalElements(),
            products.getTotalPages());
    }
}
