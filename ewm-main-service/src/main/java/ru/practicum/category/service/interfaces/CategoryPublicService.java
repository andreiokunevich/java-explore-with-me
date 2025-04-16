package ru.practicum.category.service.interfaces;

import ru.practicum.category.dto.CategoryDto;

import java.util.List;

public interface CategoryPublicService {
    CategoryDto getCategoryById(Long catId);

    List<CategoryDto> getAllCategories(int from, int size);
}