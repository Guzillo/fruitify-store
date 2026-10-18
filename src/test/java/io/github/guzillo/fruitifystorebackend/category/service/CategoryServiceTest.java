package io.github.guzillo.fruitifystorebackend.category.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.guzillo.fruitifystorebackend.category.Category;
import io.github.guzillo.fruitifystorebackend.category.CategoryRepository;
import io.github.guzillo.fruitifystorebackend.category.dto.CategoryResponseDTO;
import io.github.guzillo.fruitifystorebackend.category.mapper.CategoryMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void shouldReturnMappedCategories() {
        Category fruits = new Category();
        fruits.setId(1L);
        CategoryResponseDTO response = new CategoryResponseDTO(1L, "Fruits", "fruits");
        when(categoryRepository.findAll()).thenReturn(List.of(fruits));
        when(categoryMapper.toResponse(fruits)).thenReturn(response);

        assertThat(categoryService.findCategories()).containsExactly(response);

        verify(categoryRepository).findAll();
        verify(categoryMapper).toResponse(fruits);
    }
}
