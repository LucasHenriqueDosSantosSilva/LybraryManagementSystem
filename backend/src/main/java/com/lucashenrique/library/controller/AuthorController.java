package com.lucashenrique.library.controller;
import com.lucashenrique.library.dto.*;
import com.lucashenrique.library.service.AuthorService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.net.URI;
@RestController
@RequestMapping("/api/authors")
public class AuthorController {
    private final AuthorService service;
    public AuthorController(AuthorService service) { this.service = service; }
    @PostMapping
    public ResponseEntity<AuthorResponse> create(@Valid @RequestBody AuthorRequest request) {
        AuthorResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/authors/" + response.id())).body(response);
    }
    @GetMapping
    public Page<AuthorResponse> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        if (page < 0 || size < 1 || size > 100) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Paginação inválida.");
        return service.list(page, size);
    }
    @GetMapping("/{id}") public AuthorResponse get(@PathVariable Long id) { return service.get(id); }
    @PutMapping("/{id}") public AuthorResponse update(@PathVariable Long id, @Valid @RequestBody AuthorRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
