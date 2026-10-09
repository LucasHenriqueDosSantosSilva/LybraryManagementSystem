package com.lucashenrique.library.controller;

import com.lucashenrique.library.api.PageParameters;
import com.lucashenrique.library.dto.*;
import com.lucashenrique.library.service.BookCopyService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/copies")
public class BookCopyController {
  private final BookCopyService service;

  public BookCopyController(BookCopyService service) {
    this.service = service;
  }

  @PostMapping
  public ResponseEntity<CopyResponse> create(@Valid @RequestBody CopyRequest request) {
    CopyResponse response = service.create(request);
    return ResponseEntity.created(URI.create("/api/copies/" + response.id())).body(response);
  }

  @GetMapping
  public Page<CopyResponse> list(
      @RequestParam(defaultValue = PageParameters.DEFAULT_PAGE) int page,
      @RequestParam(defaultValue = PageParameters.DEFAULT_SIZE) int size) {
    PageParameters pagination = new PageParameters(page, size);
    return service.list(pagination.page(), pagination.size());
  }

  @GetMapping("/{id}")
  public CopyResponse get(@PathVariable Long id) {
    return service.get(id);
  }

  @PutMapping("/{id}")
  public CopyResponse update(@PathVariable Long id, @Valid @RequestBody CopyRequest request) {
    return service.update(id, request);
  }

  @PostMapping("/{id}/withdrawal")
  public CopyResponse withdraw(@PathVariable Long id) {
    return service.withdraw(id);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
