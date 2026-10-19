package io.github.guzillo.fruitifystorebackend.category.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.guzillo.fruitifystorebackend.category.Category;
import io.github.guzillo.fruitifystorebackend.category.CategoryRepository;
import io.github.guzillo.fruitifystorebackend.category.dto.CategoryCreateRequestDTO;
import io.github.guzillo.fruitifystorebackend.category.dto.CategoryResponseDTO;
import io.github.guzillo.fruitifystorebackend.category.exception.CategoryAlreadyExistsException;
import io.github.guzillo.fruitifystorebackend.category.exception.CategoryInUseException;
import io.github.guzillo.fruitifystorebackend.category.mapper.CategoryMapper;
import io.github.guzillo.fruitifystorebackend.common.exception.ResourceNotFoundException;
import io.github.guzillo.fruitifystorebackend.product.model.Product;
import java.util.List;
import java.util.Optional;
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

    @Test
    void shouldCreateCategory() {
        CategoryCreateRequestDTO request = new CategoryCreateRequestDTO("Fruits", "fruits");
        Category category = new Category();
        CategoryResponseDTO response = new CategoryResponseDTO(1L, "Fruits", "fruits");

        when(categoryRepository.existsByName("Fruits")).thenReturn(false);
        when(categoryMapper.toEntity(request)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toResponse(category)).thenReturn(response);

        assertThat(categoryService.createCategory(request)).isEqualTo(response);

        verify(categoryRepository).save(category);
    }

    @Test
    void shouldRejectCategoryWithDuplicateName() {
        CategoryCreateRequestDTO request = new CategoryCreateRequestDTO("Fruits", "fruits");

        when(categoryRepository.existsByName("Fruits")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(request))
            .isInstanceOf(CategoryAlreadyExistsException.class)
            .hasMessage("Category already exists: Fruits");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldDeleteEmptyCategory() {
        Category category = new Category();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        categoryService.deleteCategory(1L);

        verify(categoryRepository).delete(category);
    }

    @Test
    void shouldRejectDeletingCategoryWithProducts() {
        Category category = new Category();
        category.getProducts().add(new Product());
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> categoryService.deleteCategory(1L))
            .isInstanceOf(CategoryInUseException.class)
            .hasMessage("Category is still used by products: 1");

        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void shouldThrowWhenDeletingMissingCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory(1L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Category was not found: 1");

        verify(categoryRepository, never()).delete(any());
    }
}
