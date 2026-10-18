package io.github.guzillo.fruitifystorebackend.category.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.guzillo.fruitifystorebackend.category.dto.CategoryResponseDTO;
import io.github.guzillo.fruitifystorebackend.category.service.CategoryService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController)
            .setMessageConverters(new JacksonJsonHttpMessageConverter())
            .build();
    }

    @Test
    void shouldReturnCategories() throws Exception {
        when(categoryService.findCategories()).thenReturn(List.of(
            new CategoryResponseDTO(1L, "Fruits", "fruits"),
            new CategoryResponseDTO(2L, "Vegetables", "vegetables")));

        mockMvc.perform(get("/api/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Fruits"))
            .andExpect(jsonPath("$[0].slug").value("fruits"))
            .andExpect(jsonPath("$[1].id").value(2));

        verify(categoryService).findCategories();
    }
}
