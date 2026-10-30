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
import io.github.guzillo.fruitifystorebackend.product.dto.ProductUpdateRequestDTO;
import io.github.guzillo.fruitifystorebackend.product.exception.ProductAlreadyExistsException;
import io.github.guzillo.fruitifystorebackend.product.exception.ProductInUseException;
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

        ProductResponseDTO result = productService.findProduct(1L, true);

        assertThat(result).isEqualTo(response);
        verify(productRepository).findWithCategoryById(1L);
        verify(productMapper).toResponse(product);
    }

    @Test
    void shouldThrowWhenProductDoesNotExist() {
        when(productRepository.findWithCategoryById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findProduct(1L, true))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Product was not found: 1");

        verify(productMapper, never()).toResponse(any());
    }

    @Test
    void shouldReturnPagedProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Product product = new Product();
        ProductResponseDTO response = productResponse(1L, "Orange");

        when(productRepository.findAllByIsActiveTrue(pageable))
            .thenReturn(new PageImpl<>(List.of(product)));
        when(productMapper.toResponse(product)).thenReturn(response);

        Page<ProductResponseDTO> result = productService.findProducts(pageable, null, false);

        assertThat(result.getContent()).containsExactly(response);
        verify(productRepository).findAllByIsActiveTrue(pageable);
        verify(productMapper).toResponse(product);
    }

    @Test
    void shouldReturnPagedProductsFilteredByCategory() {
        Pageable pageable = PageRequest.of(0, 10);
        Product product = new Product();
        ProductResponseDTO response = productResponse(1L, "Orange");

        when(productRepository.findAllByCategoryIdAndIsActiveTrue(1L, pageable))
            .thenReturn(new PageImpl<>(List.of(product)));
        when(productMapper.toResponse(product)).thenReturn(response);

        Page<ProductResponseDTO> result = productService.findProducts(pageable, 1L, false);

        assertThat(result.getContent()).containsExactly(response);
        verify(productRepository).findAllByCategoryIdAndIsActiveTrue(1L, pageable);
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

    @Test
    void shouldUpdateProvidedProductFields() {
        ProductUpdateRequestDTO request = new ProductUpdateRequestDTO(
            "Updated Apple", new BigDecimal("3.49"), "Updated description", 50, false, 2L,
            "https://example.test/updated-apple.png");
        Product product = new Product();
        product.setPrice(new BigDecimal("2.49"));
        product.setDescription("Old description");
        product.setStockQuantity(10);
        product.setIsActive(true);
        Category category = new Category();
        Category updatedCategory = new Category();
        product.setCategory(category);
        ProductResponseDTO response = productResponse(1L, "Apple");

        when(productRepository.findWithCategoryById(1L)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(updatedCategory));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(response);

        assertThat(productService.updateProduct(1L, request)).isEqualTo(response);

        assertThat(product.getPrice()).isEqualByComparingTo("3.49");
        assertThat(product.getDescription()).isEqualTo("Updated description");
        assertThat(product.getStockQuantity()).isEqualTo(50);
        assertThat(product.getIsActive()).isFalse();
        assertThat(product.getCategory()).isSameAs(updatedCategory);
        assertThat(product.getImageUrl())
            .isEqualTo("https://example.test/updated-apple.png");
        verify(categoryRepository).findById(2L);
        verify(productRepository).save(product);
        verify(productMapper).toResponse(product);
    }

    @Test
    void shouldUpdateOnlyProvidedFields() {
        ProductUpdateRequestDTO request = new ProductUpdateRequestDTO(
            null, null, null, 50, null, null, null);
        Product product = new Product();
        product.setPrice(new BigDecimal("2.49"));
        product.setDescription("Description");
        product.setStockQuantity(10);

        when(productRepository.findWithCategoryById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(productResponse(1L, "Apple"));

        productService.updateProduct(1L, request);

        assertThat(product.getPrice()).isEqualByComparingTo("2.49");
        assertThat(product.getDescription()).isEqualTo("Description");
        assertThat(product.getStockQuantity()).isEqualTo(50);
    }

    @Test
    void shouldRejectDuplicateNameWhenUpdatingProduct() {
        ProductUpdateRequestDTO request = new ProductUpdateRequestDTO(
            "Orange", null, null, null, null, null, null);
        Product product = new Product();
        product.setName("Apple");

        when(productRepository.findWithCategoryById(1L)).thenReturn(Optional.of(product));
        when(productRepository.existsByNameAndIdNot("Orange", 1L)).thenReturn(true);

        assertThatThrownBy(() -> productService.updateProduct(1L, request))
            .isInstanceOf(ProductAlreadyExistsException.class)
            .hasMessage("Product already exists: Orange");

        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenUpdatingMissingProduct() {
        when(productRepository.findWithCategoryById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(
            1L, new ProductUpdateRequestDTO(
                null, new BigDecimal("3.49"), null, null, null, null, null)))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Product was not found: 1");
    }

    @Test
    void shouldDeleteExistingProduct() {
        Product product = new Product();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.deleteProduct(1L);

        verify(productRepository).delete(product);
    }

    @Test
    void shouldRefuseToDeleteProductThatIsPartOfOrders() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(new Product()));
        when(productRepository.isInAnyOrder(1L)).thenReturn(true);

        assertThatThrownBy(() -> productService.deleteProduct(1L))
            .isInstanceOf(ProductInUseException.class);

        verify(productRepository, never()).delete(any());
    }

    @Test
    void shouldThrowWhenDeletingMissingProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteProduct(1L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Product was not found: 1");

        verify(productRepository, never()).delete(any());
    }

    private ProductResponseDTO productResponse(Long id, String name) {
        return new ProductResponseDTO(
            id,
            name,
            "Juicy fruit",
            new BigDecimal("2.49"),
            25,
            "https://example.test/" + name.toLowerCase() + ".png",
            new CategoryResponseDTO(1L, "Fruits", "fruits"), true);
    }
}
