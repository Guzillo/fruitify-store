package io.github.guzillo.fruitifystorebackend.category.service;

import io.github.guzillo.fruitifystorebackend.category.Category;
import io.github.guzillo.fruitifystorebackend.category.CategoryRepository;
import io.github.guzillo.fruitifystorebackend.category.dto.CategoryCreateRequestDTO;
import io.github.guzillo.fruitifystorebackend.category.dto.CategoryResponseDTO;
import io.github.guzillo.fruitifystorebackend.category.exception.CategoryAlreadyExistsException;
import io.github.guzillo.fruitifystorebackend.category.exception.CategoryInUseException;
import io.github.guzillo.fruitifystorebackend.category.mapper.CategoryMapper;
import io.github.guzillo.fruitifystorebackend.common.exception.ResourceNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> findCategories() {
        return categoryRepository.findAll().stream()
            .map(categoryMapper::toResponse)
            .toList();
    }

    @Transactional
    public CategoryResponseDTO createCategory(CategoryCreateRequestDTO request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new CategoryAlreadyExistsException(request.name());
        }
        return categoryMapper.toResponse(
            categoryRepository.save(categoryMapper.toEntity(request)));
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        if (!category.getProducts().isEmpty()) {
            throw new CategoryInUseException(id);
        }
        categoryRepository.delete(category);
    }
}
