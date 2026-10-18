package io.github.guzillo.fruitifystorebackend.category.controller;

import io.github.guzillo.fruitifystorebackend.category.dto.CategoryResponseDTO;
import io.github.guzillo.fruitifystorebackend.category.service.CategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryResponseDTO> getCategories() {
        return categoryService.findCategories();
    }
}
