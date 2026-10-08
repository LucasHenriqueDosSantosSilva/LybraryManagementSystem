package com.lucashenrique.library.service;
import com.lucashenrique.library.dto.*;
import com.lucashenrique.library.entity.BookCopy;
import com.lucashenrique.library.repository.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.Locale;
@Service @Transactional(isolation=org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
public class BookCopyService {
 private final BookCopyRepository copies;private final BookRepository books;private final LoanRepository loans;
 public BookCopyService(BookCopyRepository copies,BookRepository books,LoanRepository loans){this.copies=copies;this.books=books;this.loans=loans;}
 public CopyResponse create(CopyRequest request){
  var book=books.findById(request.bookId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Livro não encontrado."));
  var copy=new BookCopy(request.inventoryCode().strip().toUpperCase(Locale.ROOT),book);copies.saveAndFlush(copy);return response(copy);
 }
 public CopyResponse update(Long id,CopyRequest request){
  var copy=locked(id);
  if(!copy.getBook().getId().equals(request.bookId()))throw new ResponseStatusException(HttpStatus.CONFLICT,"O livro de um exemplar não pode ser alterado.");
  copy.setInventoryCode(request.inventoryCode().strip().toUpperCase(Locale.ROOT));copies.flush();return response(copy);
 }
 public CopyResponse withdraw(Long id){var copy=locked(id);if(loans.existsByCopyIdAndReturnedDateIsNull(id))throw new ResponseStatusException(HttpStatus.CONFLICT,"Exemplar possui empréstimo ativo.");copy.withdraw();copies.flush();return response(copy);}
 @Transactional(readOnly=true) public CopyResponse get(Long id){return response(find(id));}
 @Transactional(readOnly=true) public Page<CopyResponse> list(int page,int size){return copies.findAll(PageRequest.of(page,size,Sort.by("id"))).map(this::response);}
 public void delete(Long id){var copy=locked(id);if(loans.existsByCopyId(id))throw new ResponseStatusException(HttpStatus.CONFLICT,"Exemplar com histórico não pode ser excluído.");copies.delete(copy);copies.flush();}
 private BookCopy find(Long id){return copies.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Exemplar não encontrado."));}
 private BookCopy locked(Long id){
  Long bookId=copies.bookId(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Exemplar não encontrado."));
  books.locked(bookId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Livro não encontrado."));
  return copies.locked(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Exemplar não encontrado."));
 }
 private CopyResponse response(BookCopy copy){return new CopyResponse(copy.getId(),copy.getInventoryCode(),copy.getBook().getId(),copy.getCirculationStatus(),"IN_CIRCULATION".equals(copy.getCirculationStatus())&&!loans.existsByCopyIdAndReturnedDateIsNull(copy.getId()));}
}
