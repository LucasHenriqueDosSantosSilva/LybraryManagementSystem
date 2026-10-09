package com.lucashenrique.library.service;

import com.lucashenrique.library.dto.CategoryRequest;
import com.lucashenrique.library.dto.CategoryResponse;
import com.lucashenrique.library.entity.Category;
import com.lucashenrique.library.exception.ResourceNotFoundException;
import com.lucashenrique.library.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CategoryService {
  private final CategoryRepository repository;

  public CategoryService(CategoryRepository repository) {
    this.repository = repository;
  }

  public CategoryResponse create(CategoryRequest request) {
    Category category = repository.saveAndFlush(new Category(request.name().strip()));
    return new CategoryResponse(category.getId(), category.getName());
  }

  @Transactional(readOnly = true)
  public Page<CategoryResponse> list(int page, int size) {
    return repository
        .findAll(PageRequest.of(page, size, Sort.by("id")))
        .map(c -> new CategoryResponse(c.getId(), c.getName()));
  }

  @Transactional(readOnly = true)
  public CategoryResponse get(Long id) {
    Category c =
        repository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada."));
    return new CategoryResponse(c.getId(), c.getName());
  }

  public CategoryResponse update(Long id, CategoryRequest request) {
    Category c =
        repository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada."));
    c.setName(request.name().strip());
    repository.flush();
    return new CategoryResponse(c.getId(), c.getName());
  }

  public void delete(Long id) {
    Category c =
        repository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada."));
    repository.delete(c);
    repository.flush();
  }
}
