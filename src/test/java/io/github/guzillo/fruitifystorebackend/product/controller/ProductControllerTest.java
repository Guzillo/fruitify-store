package io.github.guzillo.fruitifystorebackend.product.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.guzillo.fruitifystorebackend.product.dto.CategoryResponseDTO;
import io.github.guzillo.fruitifystorebackend.product.dto.ProductResponseDTO;
import io.github.guzillo.fruitifystorebackend.product.service.ProductService;
import java.math.BigDecimal;
import java.util.List;
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
            true,
            "https://example.test/shoes.png",
            new CategoryResponseDTO(1L, "Obuwie", "obuwie"));
        Page<ProductResponseDTO> response = new PageImpl<>(List.of(product));
        when(productService.findProducts(org.mockito.ArgumentMatchers.any(Pageable.class)))
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

        verify(productService).findProducts(org.mockito.ArgumentMatchers.argThat(pageable ->
            pageable.getPageNumber() == 0
                && pageable.getPageSize() == 10
                && pageable.getSort().getOrderFor("price").isDescending()));
    }

    @Test
    void shouldUseSpringDefaultsWithoutOptionalParameters() throws Exception {
        when(productService.findProducts(org.mockito.ArgumentMatchers.any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/products"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isEmpty());

        verify(productService).findProducts(org.mockito.ArgumentMatchers.argThat(
            pageable -> pageable.isPaged() && pageable.getPageNumber() == 0
                && pageable.getPageSize() == 20 && pageable.getSort().isUnsorted()));
    }

}
