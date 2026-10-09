package com.lucashenrique.library.service;

import com.lucashenrique.library.dto.AuthorResponse;
import com.lucashenrique.library.dto.BookRequest;
import com.lucashenrique.library.dto.BookResponse;
import com.lucashenrique.library.entity.Author;
import com.lucashenrique.library.entity.Book;
import com.lucashenrique.library.entity.Category;
import com.lucashenrique.library.exception.BusinessConflictException;
import com.lucashenrique.library.exception.InvalidInputException;
import com.lucashenrique.library.exception.ResourceNotFoundException;
import com.lucashenrique.library.repository.AuthorRepository;
import com.lucashenrique.library.repository.BookRepository;
import com.lucashenrique.library.repository.CategoryRepository;
import com.lucashenrique.library.repository.LoanRepository;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(isolation = Isolation.READ_COMMITTED)
public class BookService {
  private final BookRepository books;
  private final LoanRepository loans;
  private final AuthorRepository authors;
  private final CategoryRepository categories;

  public BookService(
      BookRepository books,
      AuthorRepository authors,
      CategoryRepository categories,
      LoanRepository loans) {
    this.loans = loans;
    this.books = books;
    this.authors = authors;
    this.categories = categories;
  }

  public BookResponse create(BookRequest request) {
    Book book = new Book();
    apply(book, request);
    books.saveAndFlush(book);
    return response(book);
  }

  public BookResponse update(Long id, BookRequest request) {
    Book book =
        books.locked(id).orElseThrow(() -> new ResourceNotFoundException("Livro não encontrado."));
    String normalized;
    try {
      normalized = Isbn.canonicalize(request.isbn());
    } catch (IllegalArgumentException ex) {
      throw new InvalidInputException(ex.getMessage());
    }
    if (!book.getIsbn().equals(normalized) && loans.existsByCopyBookId(id))
      throw new BusinessConflictException("ISBN não pode mudar em livro com histórico.");
    apply(book, request);
    books.flush();
    return response(book);
  }

  @Transactional(readOnly = true)
  public BookResponse get(Long id) {
    return response(
        books
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Livro não encontrado.")));
  }

  @Transactional(readOnly = true)
  public Page<BookResponse> list(
      int page, int size, String title, String isbn, Long categoryId, String author) {
    if (categoryId != null && categoryId < 1)
      throw new InvalidInputException("Categoria inválida.");
    String canonical = null;
    if (isbn != null && !isbn.isBlank())
      try {
        canonical = Isbn.canonicalize(isbn);
      } catch (IllegalArgumentException ex) {
        throw new InvalidInputException(ex.getMessage());
      }
    return books
        .search(filter(title), canonical, categoryId, filter(author), PageRequest.of(page, size))
        .map(this::response);
  }

  private String filter(String value) {
    return value == null || value.isBlank() ? null : value.strip();
  }

  public void delete(Long id) {
    Book book =
        books.locked(id).orElseThrow(() -> new ResourceNotFoundException("Livro não encontrado."));
    books.delete(book);
    books.flush();
  }

  private void apply(Book book, BookRequest request) {
    String isbn;
    try {
      isbn = Isbn.canonicalize(request.isbn());
    } catch (IllegalArgumentException ex) {
      throw new InvalidInputException(ex.getMessage());
    }
    Category category =
        categories
            .findById(request.categoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada."));
    Set<Author> selected = new LinkedHashSet<>(authors.findAllById(request.authorIds()));
    if (selected.size() != request.authorIds().size())
      throw new ResourceNotFoundException("Autor não encontrado.");
    String description = request.description() == null ? null : request.description().strip();
    if (description != null && description.isEmpty()) description = null;
    book.setDetails(
        isbn,
        request.title().strip(),
        description,
        request.publicationYear().shortValue(),
        category,
        selected);
  }

  private BookResponse response(Book book) {
    return new BookResponse(
        book.getId(),
        book.getIsbn(),
        book.getTitle(),
        book.getDescription(),
        book.getPublicationYear(),
        book.getCategory().getId(),
        book.getAuthors().stream()
            .sorted(Comparator.comparing(Author::getId))
            .map(a -> new AuthorResponse(a.getId(), a.getName()))
            .toList());
  }
}
