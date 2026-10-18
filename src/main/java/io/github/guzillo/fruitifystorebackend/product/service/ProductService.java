package io.github.guzillo.fruitifystorebackend.product.service;

import io.github.guzillo.fruitifystorebackend.common.exception.ResourceNotFoundException;
import io.github.guzillo.fruitifystorebackend.product.dto.ProductResponseDTO;
import io.github.guzillo.fruitifystorebackend.product.mapper.ProductMapper;
import io.github.guzillo.fruitifystorebackend.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> findProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
            .map(productMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO findProduct(Long id) {
        return productRepository.findWithCategoryById(id)
            .map(productMapper::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }
}
