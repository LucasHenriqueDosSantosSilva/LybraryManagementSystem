package com.lucashenrique.library.readers.dto;

import jakarta.validation.constraints.NotNull;

public record ReaderStatusRequest(@NotNull Boolean active) {}
