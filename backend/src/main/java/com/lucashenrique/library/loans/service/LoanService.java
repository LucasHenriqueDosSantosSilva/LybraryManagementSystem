package com.lucashenrique.library.loans.service;

import com.lucashenrique.library.catalog.repository.BookCopyRepository;
import com.lucashenrique.library.catalog.repository.BookRepository;
import com.lucashenrique.library.exception.BusinessConflictException;
import com.lucashenrique.library.exception.InvalidInputException;
import com.lucashenrique.library.exception.ResourceNotFoundException;
import com.lucashenrique.library.loans.domain.LoanStatus;
import com.lucashenrique.library.loans.dto.LoanRequest;
import com.lucashenrique.library.loans.dto.LoanResponse;
import com.lucashenrique.library.loans.entity.Loan;
import com.lucashenrique.library.loans.repository.LoanRepository;
import com.lucashenrique.library.readers.repository.ReaderRepository;
import java.time.Clock;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(isolation = Isolation.READ_COMMITTED)
public class LoanService {
  private final LoanRepository loans;
  private final ReaderRepository readers;
  private final BookCopyRepository copies;
  private final BookRepository books;
  private final Clock clock;
  private final int days;

  public LoanService(
      LoanRepository loans,
      ReaderRepository readers,
      BookCopyRepository copies,
      BookRepository books,
      Clock clock,
      @Value("${library.loan-days:14}") int days) {
    if (days < 1 || days > 365)
      throw new IllegalArgumentException("Prazo deve ser de 1 a 365 dias.");
    this.loans = loans;
    this.readers = readers;
    this.copies = copies;
    this.books = books;
    this.clock = clock;
    this.days = days;
  }

  public LoanResponse create(LoanRequest request) {
    var reader = readers.locked(request.readerId()).orElseThrow(() -> missing("Leitor"));
    if (!reader.isActive()) throw conflict("Leitor inativo não pode realizar empréstimo.");
    Long bookId = copies.bookId(request.copyId()).orElseThrow(() -> missing("Exemplar"));
    books.locked(bookId).orElseThrow(() -> missing("Livro"));
    var copy = copies.locked(request.copyId()).orElseThrow(() -> missing("Exemplar"));
    if (!"IN_CIRCULATION".equals(copy.getCirculationStatus())
        || loans.existsByCopyIdAndReturnedDateIsNull(copy.getId()))
      throw conflict("Exemplar indisponível.");
    LocalDate today = LocalDate.now(clock);
    Loan loan = loans.saveAndFlush(new Loan(reader, copy, today, today.plusDays(days)));
    return response(loan, today);
  }

  public LoanResponse returnLoan(Long id) {
    Long copyId = loans.copyId(id).orElseThrow(() -> missing("Empréstimo"));
    Long bookId = copies.bookId(copyId).orElseThrow(() -> missing("Exemplar"));
    books.locked(bookId).orElseThrow(() -> missing("Livro"));
    copies.locked(copyId).orElseThrow(() -> missing("Exemplar"));
    Loan loan = loans.locked(id).orElseThrow(() -> missing("Empréstimo"));
    if (loan.getReturnedDate() != null) throw conflict("Empréstimo já devolvido.");
    LocalDate today = LocalDate.now(clock);
    if (today.isBefore(loan.getLoanDate()))
      throw conflict("A devolução não pode preceder o empréstimo.");
    loan.returnOn(today);
    loans.flush();
    return response(loan, today);
  }

  @Transactional(readOnly = true)
  public LoanResponse get(Long id) {
    return response(
        loans.findById(id).orElseThrow(() -> missing("Empréstimo")), LocalDate.now(clock));
  }

  @Transactional(readOnly = true)
  public Page<LoanResponse> list(Long readerId, String status, int page, int size) {
    if (readerId != null && !readers.existsById(readerId)) throw missing("Leitor");
    if (status != null && !java.util.Set.of("ACTIVE", "RETURNED", "OVERDUE").contains(status))
      throw new InvalidInputException("Filtro de status inválido.");
    LocalDate today = LocalDate.now(clock);
    return loans
        .search(
            readerId,
            status,
            today,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "loanDate", "id")))
        .map(l -> response(l, today));
  }

  private LoanResponse response(Loan l, LocalDate today) {
    String status = LoanStatus.classify(l.getDueDate(), l.getReturnedDate(), today).name();
    return new LoanResponse(
        l.getId(),
        l.getReader().getId(),
        l.getCopy().getId(),
        l.getCopy().getBook().getId(),
        l.getLoanDate(),
        l.getDueDate(),
        l.getReturnedDate(),
        status);
  }

  private ResourceNotFoundException missing(String name) {
    return new ResourceNotFoundException(name + " não encontrado.");
  }

  private BusinessConflictException conflict(String message) {
    return new BusinessConflictException(message);
  }
}
