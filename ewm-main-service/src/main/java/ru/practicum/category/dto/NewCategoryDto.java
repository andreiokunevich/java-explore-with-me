package ru.practicum.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewCategoryDto {
    @NotBlank(message = "Имя категории не может быть пустым.")
    @Size(min = 1, max = 50, message = "Минимальная длина имени категории 1 символ, максимальная - 50.")
    private String name;
}