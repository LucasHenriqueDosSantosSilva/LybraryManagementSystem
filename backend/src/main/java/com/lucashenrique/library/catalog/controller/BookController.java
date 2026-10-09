package com.lucashenrique.library.catalog.controller;

import com.lucashenrique.library.api.PageParameters;
import com.lucashenrique.library.catalog.dto.BookRequest;
import com.lucashenrique.library.catalog.dto.BookResponse;
import com.lucashenrique.library.catalog.service.BookService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
public class BookController {
  private final BookService service;

  public BookController(BookService service) {
    this.service = service;
  }

  @PostMapping
  public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest request) {
    BookResponse response = service.create(request);
    return ResponseEntity.created(URI.create("/api/books/" + response.id())).body(response);
  }

  @GetMapping
  public Page<BookResponse> list(
      @RequestParam(defaultValue = PageParameters.DEFAULT_PAGE) int page,
      @RequestParam(defaultValue = PageParameters.DEFAULT_SIZE) int size,
      @RequestParam(required = false) String title,
      @RequestParam(required = false) String isbn,
      @RequestParam(required = false) Long categoryId,
      @RequestParam(required = false) String author) {
    PageParameters pagination = new PageParameters(page, size);
    return service.list(pagination.page(), pagination.size(), title, isbn, categoryId, author);
  }

  @GetMapping("/{id}")
  public BookResponse get(@PathVariable Long id) {
    return service.get(id);
  }

  @PutMapping("/{id}")
  public BookResponse update(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
    return service.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
