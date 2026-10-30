package io.github.guzillo.fruitifystorebackend.product.service;

import io.github.guzillo.fruitifystorebackend.common.exception.ResourceNotFoundException;
import io.github.guzillo.fruitifystorebackend.category.Category;
import io.github.guzillo.fruitifystorebackend.category.CategoryRepository;
import io.github.guzillo.fruitifystorebackend.product.dto.ProductCreateRequestDTO;
import io.github.guzillo.fruitifystorebackend.product.dto.ProductResponseDTO;
import io.github.guzillo.fruitifystorebackend.product.dto.ProductUpdateRequestDTO;
import io.github.guzillo.fruitifystorebackend.product.exception.ProductAlreadyExistsException;
import io.github.guzillo.fruitifystorebackend.product.mapper.ProductMapper;
import io.github.guzillo.fruitifystorebackend.product.model.Product;
import io.github.guzillo.fruitifystorebackend.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> findProducts(Pageable pageable, Long categoryId,
        boolean includeInactive) {
        Page<Product> products;
        if (categoryId == null) {
            products = includeInactive
                ? productRepository.findAll(pageable)
                : productRepository.findAllByIsActiveTrue(pageable);
        } else {
            products = includeInactive
                ? productRepository.findAllByCategoryId(categoryId, pageable)
                : productRepository.findAllByCategoryIdAndIsActiveTrue(categoryId, pageable);
        }
        return products
            .map(productMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO findProduct(Long id, boolean includeInactive) {
        Optional<Product> product = includeInactive
            ? productRepository.findWithCategoryById(id)
            : productRepository.findWithCategoryByIdAndIsActiveTrue(id);
        return product
            .map(productMapper::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    @Transactional
    public ProductResponseDTO createProduct(ProductCreateRequestDTO request) {
        if (productRepository.existsByName(request.name())) {
            throw new ProductAlreadyExistsException(request.name());
        }

        Category category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category", request.categoryId()));
        return productMapper.toResponse(
            productRepository.save(productMapper.toEntity(request, category)));
    }

    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductUpdateRequestDTO request) {
        var product = productRepository.findWithCategoryById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        if (request.name() != null && !request.name().equals(product.getName())
            && productRepository.existsByNameAndIdNot(request.name(), id)) {
            throw new ProductAlreadyExistsException(request.name());
        }
        if (request.name() != null) {
            product.setName(request.name());
        }

        if (request.price() != null) {
            product.setPrice(request.price());
        }
        if (request.description() != null) {
            product.setDescription(request.description());
        }
        if (request.stockQuantity() != null) {
            product.setStockQuantity(request.stockQuantity());
        }
        if (request.isActive() != null) {
            product.setIsActive(request.isActive());
        }
        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Category", request.categoryId()));
            product.setCategory(category);
        }
        if (request.imageUrl() != null) {
            product.setImageUrl(request.imageUrl());
        }

        return productMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        productRepository.delete(product);
    }
}
