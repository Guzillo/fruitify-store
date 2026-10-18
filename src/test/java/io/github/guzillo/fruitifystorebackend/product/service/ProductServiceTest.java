package io.github.guzillo.fruitifystorebackend.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.guzillo.fruitifystorebackend.common.exception.ResourceNotFoundException;
import io.github.guzillo.fruitifystorebackend.category.Category;
import io.github.guzillo.fruitifystorebackend.category.CategoryRepository;
import io.github.guzillo.fruitifystorebackend.product.dto.ProductCreateRequestDTO;
import io.github.guzillo.fruitifystorebackend.category.dto.CategoryResponseDTO;
import io.github.guzillo.fruitifystorebackend.product.dto.ProductResponseDTO;
import io.github.guzillo.fruitifystorebackend.product.exception.ProductAlreadyExistsException;
import io.github.guzillo.fruitifystorebackend.product.mapper.ProductMapper;
import io.github.guzillo.fruitifystorebackend.product.model.Product;
import io.github.guzillo.fruitifystorebackend.product.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldReturnProductById() {
        Product product = new Product();
        ProductResponseDTO response = productResponse(1L, "Orange");

        when(productRepository.findWithCategoryById(1L)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(response);

        ProductResponseDTO result = productService.findProduct(1L);

        assertThat(result).isEqualTo(response);
        verify(productRepository).findWithCategoryById(1L);
        verify(productMapper).toResponse(product);
    }

    @Test
    void shouldThrowWhenProductDoesNotExist() {
        when(productRepository.findWithCategoryById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findProduct(1L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Product was not found: 1");

        verify(productMapper, never()).toResponse(any());
    }

    @Test
    void shouldReturnPagedProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Product product = new Product();
        ProductResponseDTO response = productResponse(1L, "Orange");

        when(productRepository.findAll(pageable))
            .thenReturn(new PageImpl<>(List.of(product)));
        when(productMapper.toResponse(product)).thenReturn(response);

        Page<ProductResponseDTO> result = productService.findProducts(pageable);

        assertThat(result.getContent()).containsExactly(response);
        verify(productRepository).findAll(pageable);
        verify(productMapper).toResponse(product);
    }

    @Test
    void shouldCreateProductInExistingCategory() {
        ProductCreateRequestDTO request = new ProductCreateRequestDTO(
            "Apple", "Fresh apple", new BigDecimal("2.49"), 10, true,
            "https://example.test/apple.png", 1L);
        Category category = new Category();
        Product product = new Product();
        ProductResponseDTO response = productResponse(2L, "Apple");

        when(productRepository.existsByName("Apple")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productMapper.toEntity(request, category)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(response);

        assertThat(productService.createProduct(request)).isEqualTo(response);

        verify(categoryRepository).findById(1L);
        verify(productRepository).existsByName("Apple");
        verify(productRepository).save(product);
        verify(productMapper).toEntity(request, category);
        verify(productMapper).toResponse(product);
    }

    @Test
    void shouldRejectProductWithDuplicateName() {
        ProductCreateRequestDTO request = new ProductCreateRequestDTO(
            "Apple", "Fresh apple", new BigDecimal("2.49"), 10, true,
            "https://example.test/apple.png", 1L);

        when(productRepository.existsByName("Apple")).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(request))
            .isInstanceOf(ProductAlreadyExistsException.class)
            .hasMessage("Product already exists: Apple");

        verify(categoryRepository, never()).findById(1L);
        verify(productRepository, never()).save(any());
    }

    private ProductResponseDTO productResponse(Long id, String name) {
        return new ProductResponseDTO(
            id,
            name,
            "Juicy fruit",
            new BigDecimal("2.49"),
            25,
            true,
            "https://example.test/" + name.toLowerCase() + ".png",
            new CategoryResponseDTO(1L, "Fruits", "fruits"));
    }
}
