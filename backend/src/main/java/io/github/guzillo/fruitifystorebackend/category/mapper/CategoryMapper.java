package io.github.guzillo.fruitifystorebackend.category.mapper;

import io.github.guzillo.fruitifystorebackend.category.Category;
import io.github.guzillo.fruitifystorebackend.category.dto.CategoryCreateRequestDTO;
import io.github.guzillo.fruitifystorebackend.category.dto.CategoryResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toEntity(CategoryCreateRequestDTO request) {
        Category category = new Category();
        category.setName(request.name());
        category.setSlug(request.slug());
        return category;
    }

    public CategoryResponseDTO toResponse(Category category) {
        return new CategoryResponseDTO(category.getId(), category.getName(), category.getSlug());
    }
}
