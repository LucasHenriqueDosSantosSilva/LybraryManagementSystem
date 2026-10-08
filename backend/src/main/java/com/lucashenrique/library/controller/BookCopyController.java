package com.lucashenrique.library.controller;
import com.lucashenrique.library.dto.*;
import com.lucashenrique.library.service.BookCopyService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.net.URI;
@RestController
@RequestMapping("/api/copies")
public class BookCopyController {
    private final BookCopyService service;
    public BookCopyController(BookCopyService service) { this.service = service; }
    @PostMapping
    public ResponseEntity<CopyResponse> create(@Valid @RequestBody CopyRequest request) {
        CopyResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/copies/" + response.id())).body(response);
    }
    @GetMapping
    public Page<CopyResponse> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        if (page < 0 || size < 1 || size > 100) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Paginação inválida.");
        return service.list(page, size);
    }
    @GetMapping("/{id}") public CopyResponse get(@PathVariable Long id) { return service.get(id); }
    @PutMapping("/{id}") public CopyResponse update(@PathVariable Long id, @Valid @RequestBody CopyRequest request) { return service.update(id, request); }
    @PostMapping("/{id}/withdrawal") public CopyResponse withdraw(@PathVariable Long id) { return service.withdraw(id); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
