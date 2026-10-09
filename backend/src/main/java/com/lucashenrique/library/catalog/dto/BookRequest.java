package com.lucashenrique.library.catalog.dto;

import jakarta.validation.constraints.*;
import java.util.Set;

public record BookRequest(
    @NotBlank @Size(max = 30) String isbn,
    @NotBlank @Size(max = 250) String title,
    @Size(max = 5000) String description,
    @NotNull @Min(1) @Max(9999) Integer publicationYear,
    @NotNull @Positive Long categoryId,
    @NotEmpty Set<@NotNull @Positive Long> authorIds) {}
