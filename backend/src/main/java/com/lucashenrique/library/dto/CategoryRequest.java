package com.lucashenrique.library.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record CategoryRequest(@NotBlank(message = "Informe o nome.") @Size(max = 100, message = "Use no máximo 100 caracteres.") String name) {}
