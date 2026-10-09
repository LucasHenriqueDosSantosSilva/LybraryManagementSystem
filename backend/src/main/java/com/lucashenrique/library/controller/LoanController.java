package com.lucashenrique.library.controller;

import com.lucashenrique.library.api.PageParameters;
import com.lucashenrique.library.dto.*;
import com.lucashenrique.library.service.LoanService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loans")
public class LoanController {
  private final LoanService service;

  public LoanController(LoanService service) {
    this.service = service;
  }

  @PostMapping
  public ResponseEntity<LoanResponse> create(@Valid @RequestBody LoanRequest request) {
    var result = service.create(request);
    return ResponseEntity.created(URI.create("/api/loans/" + result.id())).body(result);
  }

  @PostMapping("/{id}/return")
  public LoanResponse returnLoan(@PathVariable Long id) {
    return service.returnLoan(id);
  }

  @GetMapping("/{id}")
  public LoanResponse get(@PathVariable Long id) {
    return service.get(id);
  }

  @GetMapping
  public Page<LoanResponse> list(
      @RequestParam(required = false) Long readerId,
      @RequestParam(required = false) String status,
      @RequestParam(defaultValue = PageParameters.DEFAULT_PAGE) int page,
      @RequestParam(defaultValue = PageParameters.DEFAULT_SIZE) int size) {
    PageParameters pagination = new PageParameters(page, size);
    return service.list(readerId, status, pagination.page(), pagination.size());
  }
}
