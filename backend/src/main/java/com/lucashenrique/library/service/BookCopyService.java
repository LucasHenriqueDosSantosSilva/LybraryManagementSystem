package com.lucashenrique.library.service;

import com.lucashenrique.library.dto.CopyRequest;
import com.lucashenrique.library.dto.CopyResponse;
import com.lucashenrique.library.entity.BookCopy;
import com.lucashenrique.library.exception.BusinessConflictException;
import com.lucashenrique.library.exception.ResourceNotFoundException;
import com.lucashenrique.library.repository.BookCopyRepository;
import com.lucashenrique.library.repository.BookRepository;
import com.lucashenrique.library.repository.LoanRepository;
import java.util.Locale;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(isolation = Isolation.READ_COMMITTED)
public class BookCopyService {
  private final BookCopyRepository copies;
  private final BookRepository books;
  private final LoanRepository loans;

  public BookCopyService(BookCopyRepository copies, BookRepository books, LoanRepository loans) {
    this.copies = copies;
    this.books = books;
    this.loans = loans;
  }

  public CopyResponse create(CopyRequest request) {
    var book =
        books
            .findById(request.bookId())
            .orElseThrow(() -> new ResourceNotFoundException("Livro não encontrado."));
    var copy = new BookCopy(request.inventoryCode().strip().toUpperCase(Locale.ROOT), book);
    copies.saveAndFlush(copy);
    return response(copy);
  }

  public CopyResponse update(Long id, CopyRequest request) {
    var copy = locked(id);
    if (!copy.getBook().getId().equals(request.bookId()))
      throw new BusinessConflictException("O livro de um exemplar não pode ser alterado.");
    copy.setInventoryCode(request.inventoryCode().strip().toUpperCase(Locale.ROOT));
    copies.flush();
    return response(copy);
  }

  public CopyResponse withdraw(Long id) {
    var copy = locked(id);
    if (loans.existsByCopyIdAndReturnedDateIsNull(id))
      throw new BusinessConflictException("Exemplar possui empréstimo ativo.");
    copy.withdraw();
    copies.flush();
    return response(copy);
  }

  @Transactional(readOnly = true)
  public CopyResponse get(Long id) {
    return response(find(id));
  }

  @Transactional(readOnly = true)
  public Page<CopyResponse> list(int page, int size) {
    Page<BookCopy> result = copies.findAll(PageRequest.of(page, size, Sort.by("id")));
    Set<Long> occupied =
        result.isEmpty()
            ? Set.of()
            : Set.copyOf(
                loans.activeCopyIds(result.getContent().stream().map(BookCopy::getId).toList()));
    return result.map(
        copy ->
            response(
                copy,
                "IN_CIRCULATION".equals(copy.getCirculationStatus())
                    && !occupied.contains(copy.getId())));
  }

  public void delete(Long id) {
    var copy = locked(id);
    if (loans.existsByCopyId(id))
      throw new BusinessConflictException("Exemplar com histórico não pode ser excluído.");
    copies.delete(copy);
    copies.flush();
  }

  private BookCopy find(Long id) {
    return copies
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Exemplar não encontrado."));
  }

  private BookCopy locked(Long id) {
    Long bookId =
        copies
            .bookId(id)
            .orElseThrow(() -> new ResourceNotFoundException("Exemplar não encontrado."));
    books.locked(bookId).orElseThrow(() -> new ResourceNotFoundException("Livro não encontrado."));
    return copies
        .locked(id)
        .orElseThrow(() -> new ResourceNotFoundException("Exemplar não encontrado."));
  }

  private CopyResponse response(BookCopy copy) {
    boolean available =
        "IN_CIRCULATION".equals(copy.getCirculationStatus())
            && !loans.existsByCopyIdAndReturnedDateIsNull(copy.getId());
    return response(copy, available);
  }

  private CopyResponse response(BookCopy copy, boolean available) {
    return new CopyResponse(
        copy.getId(),
        copy.getInventoryCode(),
        copy.getBook().getId(),
        copy.getCirculationStatus(),
        available);
  }
}
