package com.lucashenrique.library.catalog.dto;

import java.util.List;

public record BookResponse(
    Long id,
    String isbn,
    String title,
    String description,
    int publicationYear,
    Long categoryId,
    List<AuthorResponse> authors) {}
