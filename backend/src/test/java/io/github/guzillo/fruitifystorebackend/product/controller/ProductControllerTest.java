package io.github.guzillo.fruitifystorebackend.product.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.guzillo.fruitifystorebackend.category.dto.CategoryResponseDTO;
import io.github.guzillo.fruitifystorebackend.common.exception.GlobalExceptionHandler;
import io.github.guzillo.fruitifystorebackend.common.exception.ResourceNotFoundException;
import io.github.guzillo.fruitifystorebackend.product.exception.ProductAlreadyExistsException;
import io.github.guzillo.fruitifystorebackend.product.dto.ProductResponseDTO;
import io.github.guzillo.fruitifystorebackend.product.dto.ProductCreateRequestDTO;
import io.github.guzillo.fruitifystorebackend.product.dto.ProductUpdateRequestDTO;
import io.github.guzillo.fruitifystorebackend.product.service.ProductService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
            .setMessageConverters(new JacksonJsonHttpMessageConverter())
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    void shouldReturnProductsWithPaginationAndSorting() throws Exception {
        ProductResponseDTO product = new ProductResponseDTO(
            1L,
            "Buty sportowe",
            "Lekkie buty do biegania",
            new BigDecimal("199.99"),
            12,
            "https://example.test/shoes.png",
            new CategoryResponseDTO(1L, "Obuwie", "obuwie"));
        Page<ProductResponseDTO> response = new PageImpl<>(List.of(product));
        when(productService.findProducts(
            org.mockito.ArgumentMatchers.any(Pageable.class),
            org.mockito.ArgumentMatchers.isNull(),
            org.mockito.ArgumentMatchers.anyBoolean()))
            .thenReturn(response);

        mockMvc.perform(get("/api/products")
                .queryParam("page", "0")
                .queryParam("size", "10")
                .queryParam("sort", "price,desc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value("Buty sportowe"))
            .andExpect(jsonPath("$.content[0].category.id").value(1))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.number").value(0))
            .andExpect(jsonPath("$.size").value(1));

        verify(productService).findProducts(
            org.mockito.ArgumentMatchers.argThat(pageable ->
                pageable.getPageNumber() == 0
                    && pageable.getPageSize() == 10
                    && Objects.requireNonNull(
                        pageable.getSort().getOrderFor("price")).isDescending()),
            org.mockito.ArgumentMatchers.isNull(),
            org.mockito.ArgumentMatchers.eq(false));
    }

    @Test
    void shouldListProductsSortedByNameWithPageSizeTwentyByDefault() throws Exception {
        when(productService.findProducts(
            org.mockito.ArgumentMatchers.any(Pageable.class),
            org.mockito.ArgumentMatchers.isNull(),
            org.mockito.ArgumentMatchers.anyBoolean()))
            .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/products"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isEmpty());

        verify(productService).findProducts(
            org.mockito.ArgumentMatchers.argThat(
                pageable -> pageable.isPaged() && pageable.getPageNumber() == 0
                    && pageable.getPageSize() == 20
                    && pageable.getSort().getOrderFor("name") != null
                    && pageable.getSort().getOrderFor("name").isAscending()),
            org.mockito.ArgumentMatchers.isNull(),
            org.mockito.ArgumentMatchers.eq(false));
    }

    @Test
    void shouldFilterProductsByCategory() throws Exception {
        when(productService.findProducts(
            org.mockito.ArgumentMatchers.any(Pageable.class),
            org.mockito.ArgumentMatchers.eq(2L),
            org.mockito.ArgumentMatchers.eq(false)))
            .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/products").queryParam("categoryId", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isEmpty());

        verify(productService).findProducts(
            org.mockito.ArgumentMatchers.any(Pageable.class),
            org.mockito.ArgumentMatchers.eq(2L),
            org.mockito.ArgumentMatchers.eq(false));
    }

    @Test
    void shouldRejectUnsupportedSortProperty() throws Exception {
        mockMvc.perform(get("/api/products").queryParam("sort", "category.name,asc"))
            .andExpect(status().isBadRequest());

        org.mockito.Mockito.verifyNoInteractions(productService);
    }

    @Test
    void shouldReturnProductById() throws Exception {
        ProductResponseDTO product = new ProductResponseDTO(
            1L,
            "Orange",
            "Juicy orange",
            new BigDecimal("2.49"),
            25,
            "https://example.test/orange.png",
            new CategoryResponseDTO(1L, "Fruits", "fruits"));
        when(productService.findProduct(1L, false)).thenReturn(product);

        mockMvc.perform(get("/api/products/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Orange"))
            .andExpect(jsonPath("$.description").value("Juicy orange"))
            .andExpect(jsonPath("$.price").value(2.49))
            .andExpect(jsonPath("$.stockQuantity").value(25))
            .andExpect(jsonPath("$.imageUrl").value("https://example.test/orange.png"))
            .andExpect(jsonPath("$.category.id").value(1))
            .andExpect(jsonPath("$.category.name").value("Fruits"))
            .andExpect(jsonPath("$.category.slug").value("fruits"));

        verify(productService).findProduct(1L, false);
    }

    @Test
    void shouldCreateProduct() throws Exception {
        ProductResponseDTO response = new ProductResponseDTO(
            2L, "Apple", "Fresh apple", new BigDecimal("2.49"), 10,
            "https://example.test/apple.png",
            new CategoryResponseDTO(1L, "Fruits", "fruits"));
        when(productService.createProduct(
            org.mockito.ArgumentMatchers.any(ProductCreateRequestDTO.class)))
            .thenReturn(response);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Apple",
                      "description": "Fresh apple",
                      "price": 2.49,
                      "stockQuantity": 10,
                      "isActive": true,
                      "imageUrl": "https://example.test/apple.png",
                      "categoryId": 1
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.name").value("Apple"));
    }

    @Test
    void shouldReturnConflictWhenProductNameAlreadyExists() throws Exception {
        when(productService.createProduct(
            org.mockito.ArgumentMatchers.any(ProductCreateRequestDTO.class)))
            .thenThrow(new ProductAlreadyExistsException("Apple"));

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Apple",
                      "description": "Fresh apple",
                      "price": 2.49,
                      "stockQuantity": 10,
                      "isActive": true,
                      "categoryId": 1
                    }
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$").value("Product already exists: Apple"));
    }

    @Test
    void shouldUpdateProduct() throws Exception {
        ProductResponseDTO response = new ProductResponseDTO(
            1L, "Apple", "Updated apple", new BigDecimal("3.49"), 50,
            null, new CategoryResponseDTO(1L, "Fruits", "fruits"));
        when(productService.updateProduct(
            org.mockito.ArgumentMatchers.eq(1L),
            org.mockito.ArgumentMatchers.any(ProductUpdateRequestDTO.class)))
            .thenReturn(response);

        mockMvc.perform(put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Updated Apple",
                      "price": 3.49,
                      "description": "Updated apple",
                      "stockQuantity": 50,
                      "isActive": false,
                      "categoryId": 2,
                      "imageUrl": "https://example.test/updated-apple.png"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.price").value(3.49))
            .andExpect(jsonPath("$.description").value("Updated apple"))
            .andExpect(jsonPath("$.stockQuantity").value(50));
    }

    @Test
    void shouldDeleteProduct() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
            .andExpect(status().isNoContent());

        verify(productService).deleteProduct(1L);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingMissingProduct() throws Exception {
        doThrow(new ResourceNotFoundException("Product", 1L))
            .when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/products/1"))
            .andExpect(status().isNotFound());
    }

}
