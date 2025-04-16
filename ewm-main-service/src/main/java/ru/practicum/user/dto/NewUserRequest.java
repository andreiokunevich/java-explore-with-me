package ru.practicum.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewUserRequest {
    @Size(min = 2, max = 250)
    @NotBlank(message = "Имя не может быть пустым!")
    private String name;

    @Size(min = 6, max = 254)
    @NotBlank(message = "Email должен быть указан!")
    @Email(message = "Email должен соответствовать формату - somestuff@gmail.com")
    private String email;
}