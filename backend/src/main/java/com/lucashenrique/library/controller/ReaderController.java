package com.lucashenrique.library.controller;
import com.lucashenrique.library.dto.*;
import com.lucashenrique.library.service.ReaderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.net.URI;
@RestController
@RequestMapping("/api/readers")
public class ReaderController {
    private final ReaderService service;
    public ReaderController(ReaderService service) { this.service = service; }
    @PostMapping
    public ResponseEntity<ReaderResponse> create(@Valid @RequestBody ReaderRequest request) {
        ReaderResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/readers/" + response.id())).body(response);
    }
    @GetMapping
    public Page<ReaderResponse> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        if (page < 0 || size < 1 || size > 100) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Paginação inválida.");
        return service.list(page, size);
    }
    @GetMapping("/{id}") public ReaderResponse get(@PathVariable Long id) { return service.get(id); }
    @PutMapping("/{id}") public ReaderResponse update(@PathVariable Long id, @Valid @RequestBody ReaderRequest request) { return service.update(id, request); }
    @PatchMapping("/{id}/status") public ReaderResponse status(@PathVariable Long id, @Valid @RequestBody ReaderStatusRequest request) { return service.status(id, request.active()); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
