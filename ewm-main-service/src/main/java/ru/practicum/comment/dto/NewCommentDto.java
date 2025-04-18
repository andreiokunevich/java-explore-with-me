package ru.practicum.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewCommentDto {
    @NotBlank(message = "Комментарий не может быть пустым.")
    @Size(min = 3, max = 1000, message = "Длина комментария должна быть минимум 3 символа, максимум 1000.")
    private String text;
}