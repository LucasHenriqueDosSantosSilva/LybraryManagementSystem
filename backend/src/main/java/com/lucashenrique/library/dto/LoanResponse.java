package com.lucashenrique.library.dto;
import java.time.LocalDate;
public record LoanResponse(Long id,Long readerId,Long copyId,Long bookId,LocalDate loanDate,LocalDate dueDate,LocalDate returnedDate,String status) {}
