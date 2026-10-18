package io.github.guzillo.fruitifystorebackend.category.service;

import io.github.guzillo.fruitifystorebackend.category.CategoryRepository;
import io.github.guzillo.fruitifystorebackend.category.dto.CategoryResponseDTO;
import io.github.guzillo.fruitifystorebackend.category.mapper.CategoryMapper;
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
}
