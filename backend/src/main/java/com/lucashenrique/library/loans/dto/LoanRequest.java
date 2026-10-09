package com.lucashenrique.library.loans.dto;

import jakarta.validation.constraints.*;

public record LoanRequest(@NotNull @Positive Long readerId, @NotNull @Positive Long copyId) {}
