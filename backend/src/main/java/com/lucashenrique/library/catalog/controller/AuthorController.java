package com.lucashenrique.library.catalog.controller;

import com.lucashenrique.library.api.PageParameters;
import com.lucashenrique.library.catalog.dto.AuthorRequest;
import com.lucashenrique.library.catalog.dto.AuthorResponse;
import com.lucashenrique.library.catalog.service.AuthorService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {
  private final AuthorService service;

  public AuthorController(AuthorService service) {
    this.service = service;
  }

  @PostMapping
  public ResponseEntity<AuthorResponse> create(@Valid @RequestBody AuthorRequest request) {
    AuthorResponse response = service.create(request);
    return ResponseEntity.created(URI.create("/api/authors/" + response.id())).body(response);
  }

  @GetMapping
  public Page<AuthorResponse> list(
      @RequestParam(defaultValue = PageParameters.DEFAULT_PAGE) int page,
      @RequestParam(defaultValue = PageParameters.DEFAULT_SIZE) int size) {
    PageParameters pagination = new PageParameters(page, size);
    return service.list(pagination.page(), pagination.size());
  }

  @GetMapping("/{id}")
  public AuthorResponse get(@PathVariable Long id) {
    return service.get(id);
  }

  @PutMapping("/{id}")
  public AuthorResponse update(@PathVariable Long id, @Valid @RequestBody AuthorRequest request) {
    return service.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
