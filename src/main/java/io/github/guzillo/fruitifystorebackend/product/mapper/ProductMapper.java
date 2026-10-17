package io.github.guzillo.fruitifystorebackend.product.mapper;

import io.github.guzillo.fruitifystorebackend.product.dto.CategoryResponseDTO;
import io.github.guzillo.fruitifystorebackend.product.dto.ProductResponseDTO;
import io.github.guzillo.fruitifystorebackend.product.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponseDTO toResponse(Product product) {
        return new ProductResponseDTO(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getStockQuantity(),
            product.getIsActive(),
            product.getImageUrl(),
            new CategoryResponseDTO(
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getCategory().getSlug()));
    }
}
