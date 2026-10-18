package io.github.guzillo.fruitifystorebackend.category.mapper;

import io.github.guzillo.fruitifystorebackend.category.Category;
import io.github.guzillo.fruitifystorebackend.category.dto.CategoryResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponseDTO toResponse(Category category) {
        return new CategoryResponseDTO(category.getId(), category.getName(), category.getSlug());
    }
}
