package com.lucashenrique.library.service;
import com.lucashenrique.library.dto.*;
import com.lucashenrique.library.repository.*;
import com.lucashenrique.library.entity.Loan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
@Service @Transactional(isolation=Isolation.READ_COMMITTED)
public class LoanService {
 private final LoanRepository loans;private final ReaderRepository readers;private final BookCopyRepository copies;private final BookRepository books;private final Clock clock;private final int days;
 public LoanService(LoanRepository loans,ReaderRepository readers,BookCopyRepository copies,BookRepository books,Clock clock,@Value("${library.loan-days:14}") int days){
  if(days<1||days>365)throw new IllegalArgumentException("Prazo deve ser de 1 a 365 dias.");
  this.loans=loans;this.readers=readers;this.copies=copies;this.books=books;this.clock=clock;this.days=days;
 }
 public LoanResponse create(LoanRequest request){
  var reader=readers.locked(request.readerId()).orElseThrow(()->missing("Leitor"));
  if(!reader.isActive())throw conflict("Leitor inativo não pode realizar empréstimo.");
  Long bookId=copies.bookId(request.copyId()).orElseThrow(()->missing("Exemplar"));
  books.locked(bookId).orElseThrow(()->missing("Livro"));
  var copy=copies.locked(request.copyId()).orElseThrow(()->missing("Exemplar"));
  if(!"IN_CIRCULATION".equals(copy.getCirculationStatus())||loans.existsByCopyIdAndReturnedDateIsNull(copy.getId()))throw conflict("Exemplar indisponível.");
  LocalDate today=LocalDate.now(clock);
  Loan loan=loans.saveAndFlush(new Loan(reader,copy,today,today.plusDays(days)));return response(loan,today);
 }
 public LoanResponse returnLoan(Long id){
  Long copyId=loans.copyId(id).orElseThrow(()->missing("Empréstimo"));
  Long bookId=copies.bookId(copyId).orElseThrow(()->missing("Exemplar"));
  books.locked(bookId).orElseThrow(()->missing("Livro"));copies.locked(copyId).orElseThrow(()->missing("Exemplar"));
  Loan loan=loans.locked(id).orElseThrow(()->missing("Empréstimo"));
  if(loan.getReturnedDate()!=null)throw conflict("Empréstimo já devolvido.");
  LocalDate today=LocalDate.now(clock);
  if(today.isBefore(loan.getLoanDate()))throw conflict("A devolução não pode preceder o empréstimo.");
  loan.returnOn(today);loans.flush();return response(loan,today);
 }
 @Transactional(readOnly=true) public LoanResponse get(Long id){return response(loans.findById(id).orElseThrow(()->missing("Empréstimo")),LocalDate.now(clock));}
 @Transactional(readOnly=true) public Page<LoanResponse> list(Long readerId,String status,int page,int size){
  if(readerId!=null&&!readers.existsById(readerId))throw missing("Leitor");
  if(status!=null&&!java.util.Set.of("ACTIVE","RETURNED","OVERDUE").contains(status))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Filtro de status inválido.");
  LocalDate today=LocalDate.now(clock);
  return loans.search(readerId,status,today,PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,"loanDate","id"))).map(l->response(l,today));
 }
 private LoanResponse response(Loan l,LocalDate today){
  String status=l.getReturnedDate()!=null?"RETURNED":l.getDueDate().isBefore(today)?"OVERDUE":"ACTIVE";
  return new LoanResponse(l.getId(),l.getReader().getId(),l.getCopy().getId(),l.getCopy().getBook().getId(),l.getLoanDate(),l.getDueDate(),l.getReturnedDate(),status);
 }
 private ResponseStatusException missing(String name){return new ResponseStatusException(HttpStatus.NOT_FOUND,name+" não encontrado.");}
 private ResponseStatusException conflict(String message){return new ResponseStatusException(HttpStatus.CONFLICT,message);}
}
