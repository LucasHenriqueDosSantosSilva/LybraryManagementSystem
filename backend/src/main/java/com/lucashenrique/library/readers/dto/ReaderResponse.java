package com.lucashenrique.library.readers.dto;

public record ReaderResponse(
    Long id, String registrationNumber, String name, String email, boolean active) {}
