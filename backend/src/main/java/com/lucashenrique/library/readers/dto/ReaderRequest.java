package com.lucashenrique.library.readers.dto;

import jakarta.validation.constraints.*;

public record ReaderRequest(
    @NotBlank
        @Size(max = 30)
        @Pattern(
            regexp = " *[A-Za-z0-9._-]+ *",
            message = "Use letras ASCII, números, ponto, hífen ou sublinhado.")
        String registrationNumber,
    @NotBlank @Size(max = 150) String name,
    @Email @Size(max = 254) String email) {}
