package com.lucashenrique.library.loans.dto;

import java.time.LocalDate;

public record LoanResponse(
    Long id,
    Long readerId,
    Long copyId,
    Long bookId,
    LocalDate loanDate,
    LocalDate dueDate,
    LocalDate returnedDate,
    String status) {}
