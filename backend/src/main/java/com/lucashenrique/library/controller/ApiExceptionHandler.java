package com.lucashenrique.library.controller;
import com.lucashenrique.library.exception.BusinessConflictException;
import com.lucashenrique.library.exception.InvalidInputException;
import com.lucashenrique.library.exception.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import java.sql.SQLException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ApiExceptionHandler.class);
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ProblemDetail> domain(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(ProblemDetail.forStatusAndDetail(ex.getStatusCode(), ex.getReason()));
    }
    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ProblemDetail> notFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage()));
    }
    @ExceptionHandler(BusinessConflictException.class)
    ResponseEntity<ProblemDetail> conflict(BusinessConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage()));
    }
    @ExceptionHandler(InvalidInputException.class)
    ResponseEntity<ProblemDetail> invalidInput(InvalidInputException ex) {
        return ResponseEntity.badRequest().body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ProblemDetail> integrity(DataIntegrityViolationException ex) {
        Throwable cause = ex.getMostSpecificCause();
        if (cause instanceof SQLException sql && sql.getErrorCode() == 1062 && sql.getMessage().contains("uk_categories_name")) {
            return ResponseEntity.status(409).body(ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Já existe uma categoria com esse nome."));
        }
        if (cause instanceof SQLException sql && sql.getErrorCode() == 1062 && sql.getMessage().contains("uk_books_isbn")) {
            return ResponseEntity.status(409).body(ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Já existe um livro com esse ISBN."));
        }
        if (cause instanceof SQLException sql && (sql.getErrorCode() == 1451 || sql.getErrorCode() == 1452)
                && (sql.getMessage().contains("fk_books_category") || sql.getMessage().contains("fk_book_authors_"))) {
            return ResponseEntity.status(409).body(ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "A operação conflita com vínculos do catálogo."));
        }
        if (cause instanceof SQLException sql && sql.getErrorCode() == 1062
                && (sql.getMessage().contains("uk_readers_registration") || sql.getMessage().contains("uk_copies_inventory"))) {
            return ResponseEntity.status(409).body(ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Matrícula ou código patrimonial já cadastrado."));
        }
        if (cause instanceof SQLException sql && (sql.getErrorCode() == 1451 || sql.getErrorCode() == 1452) && sql.getMessage().contains("fk_copies_book")) {
            return ResponseEntity.status(409).body(ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "O livro possui exemplares ou não está mais disponível para associação."));
        }
        if (cause instanceof SQLException sql && ((sql.getErrorCode()==1062 && sql.getMessage().contains("uk_loans_active_copy"))
                || ((sql.getErrorCode()==1451||sql.getErrorCode()==1452) && (sql.getMessage().contains("fk_loans_reader")||sql.getMessage().contains("fk_loans_copy"))))) {
            return ResponseEntity.status(409).body(ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,"A operação conflita com o histórico ou a disponibilidade do exemplar."));
        }
        LOG.error("Falha de integridade não classificada", ex);
        return ResponseEntity.internalServerError().body(ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível concluir a operação."));
    }
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Revise os campos informados.");
        body.setProperty("errors", ex.getBindingResult().getFieldErrors().stream().map(e -> Map.of("field", e.getField(), "message", e.getDefaultMessage())).toList());
        return ResponseEntity.badRequest().body(body);
    }
}
