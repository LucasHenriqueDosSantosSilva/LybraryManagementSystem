package com.lucashenrique.library.dto;
import jakarta.validation.constraints.*;
public record LoanRequest(@NotNull @Positive Long readerId,@NotNull @Positive Long copyId) {}
