package com.lucashenrique.library.dto;
import jakarta.validation.constraints.NotNull;
public record ReaderStatusRequest(@NotNull Boolean active) {}
