package com.lucashenrique.library.service;
import com.lucashenrique.library.dto.*;
import com.lucashenrique.library.entity.Author;
import com.lucashenrique.library.repository.AuthorRepository;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
@Service
@Transactional
public class AuthorService {
    private final AuthorRepository repository;
    public AuthorService(AuthorRepository repository) { this.repository = repository; }
    public AuthorResponse create(AuthorRequest request) {
        Author author = repository.saveAndFlush(new Author(request.name().strip()));
        return new AuthorResponse(author.getId(), author.getName());
    }
    @Transactional(readOnly = true)
    public Page<AuthorResponse> list(int page, int size) {
        return repository.findAll(PageRequest.of(page, size, Sort.by("id"))).map(c -> new AuthorResponse(c.getId(), c.getName()));
    }
    @Transactional(readOnly = true)
    public AuthorResponse get(Long id) {
        Author c = repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Autor não encontrada."));
        return new AuthorResponse(c.getId(), c.getName());
    }
    public AuthorResponse update(Long id, AuthorRequest request) {
        Author c = repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Autor não encontrada."));
        c.setName(request.name().strip());
        repository.flush();
        return new AuthorResponse(c.getId(), c.getName());
    }
    public void delete(Long id) {
        Author c = repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Autor não encontrada."));
        repository.delete(c);
        repository.flush();
    }
}
