package com.lucashenrique.library.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record AuthorRequest(@NotBlank(message = "Informe o nome.") @Size(max = 150, message = "Use no máximo 150 caracteres.") String name) {}
