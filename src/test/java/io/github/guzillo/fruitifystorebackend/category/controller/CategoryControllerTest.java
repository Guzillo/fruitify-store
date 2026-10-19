package io.github.guzillo.fruitifystorebackend.category.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.guzillo.fruitifystorebackend.category.dto.CategoryResponseDTO;
import io.github.guzillo.fruitifystorebackend.category.exception.CategoryAlreadyExistsException;
import io.github.guzillo.fruitifystorebackend.category.exception.CategoryInUseException;
import io.github.guzillo.fruitifystorebackend.category.service.CategoryService;
import io.github.guzillo.fruitifystorebackend.common.exception.GlobalExceptionHandler;
import io.github.guzillo.fruitifystorebackend.common.exception.ResourceNotFoundException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
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
            .setControllerAdvice(new GlobalExceptionHandler())
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

    @Test
    void shouldCreateCategory() throws Exception {
        when(categoryService.createCategory(any())).thenReturn(
            new CategoryResponseDTO(3L, "Berries", "berries"));

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name": "Berries", "slug": "berries"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(3))
            .andExpect(jsonPath("$.name").value("Berries"))
            .andExpect(jsonPath("$.slug").value("berries"));
    }

    @Test
    void shouldReturnConflictWhenCategoryNameAlreadyExists() throws Exception {
        when(categoryService.createCategory(any()))
            .thenThrow(new CategoryAlreadyExistsException("Fruits"));

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name": "Fruits", "slug": "fruits"}
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$").value("Category already exists: Fruits"));
    }

    @Test
    void shouldDeleteCategory() throws Exception {
        mockMvc.perform(delete("/api/categories/1"))
            .andExpect(status().isNoContent());

        verify(categoryService).deleteCategory(1L);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingMissingCategory() throws Exception {
        doThrow(new ResourceNotFoundException("Category", 1L))
            .when(categoryService).deleteCategory(1L);

        mockMvc.perform(delete("/api/categories/1"))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnConflictWhenDeletingCategoryWithProducts() throws Exception {
        doThrow(new CategoryInUseException(1L))
            .when(categoryService).deleteCategory(1L);

        mockMvc.perform(delete("/api/categories/1"))
            .andExpect(status().isConflict());
    }
}
