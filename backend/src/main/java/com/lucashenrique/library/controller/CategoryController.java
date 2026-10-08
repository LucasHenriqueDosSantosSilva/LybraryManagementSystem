package com.lucashenrique.library.controller;
import com.lucashenrique.library.dto.*;
import com.lucashenrique.library.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.net.URI;
@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService service;
    public CategoryController(CategoryService service) { this.service = service; }
    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/categories/" + response.id())).body(response);
    }
    @GetMapping
    public Page<CategoryResponse> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        if (page < 0 || size < 1 || size > 100) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Paginação inválida.");
        return service.list(page, size);
    }
    @GetMapping("/{id}") public CategoryResponse get(@PathVariable Long id) { return service.get(id); }
    @PutMapping("/{id}") public CategoryResponse update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
