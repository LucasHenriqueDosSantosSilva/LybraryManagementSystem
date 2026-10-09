package com.lucashenrique.library.readers.controller;

import com.lucashenrique.library.api.PageParameters;
import com.lucashenrique.library.readers.dto.ReaderRequest;
import com.lucashenrique.library.readers.dto.ReaderResponse;
import com.lucashenrique.library.readers.dto.ReaderStatusRequest;
import com.lucashenrique.library.readers.service.ReaderService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/readers")
public class ReaderController {
  private final ReaderService service;

  public ReaderController(ReaderService service) {
    this.service = service;
  }

  @PostMapping
  public ResponseEntity<ReaderResponse> create(@Valid @RequestBody ReaderRequest request) {
    ReaderResponse response = service.create(request);
    return ResponseEntity.created(URI.create("/api/readers/" + response.id())).body(response);
  }

  @GetMapping
  public Page<ReaderResponse> list(
      @RequestParam(defaultValue = PageParameters.DEFAULT_PAGE) int page,
      @RequestParam(defaultValue = PageParameters.DEFAULT_SIZE) int size) {
    PageParameters pagination = new PageParameters(page, size);
    return service.list(pagination.page(), pagination.size());
  }

  @GetMapping("/{id}")
  public ReaderResponse get(@PathVariable Long id) {
    return service.get(id);
  }

  @PutMapping("/{id}")
  public ReaderResponse update(@PathVariable Long id, @Valid @RequestBody ReaderRequest request) {
    return service.update(id, request);
  }

  @PatchMapping("/{id}/status")
  public ReaderResponse status(
      @PathVariable Long id, @Valid @RequestBody ReaderStatusRequest request) {
    return service.status(id, request.active());
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
