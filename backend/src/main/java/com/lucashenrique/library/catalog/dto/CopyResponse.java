package com.lucashenrique.library.catalog.dto;

public record CopyResponse(
    Long id, String inventoryCode, Long bookId, String circulationStatus, boolean available) {}
