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
@Service @Transactional
public class BookCopyService {
 private final BookCopyRepository copies;private final BookRepository books;
 public BookCopyService(BookCopyRepository copies,BookRepository books){this.copies=copies;this.books=books;}
 public CopyResponse create(CopyRequest request){
  var book=books.findById(request.bookId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Livro não encontrado."));
  var copy=new BookCopy(request.inventoryCode().strip().toUpperCase(Locale.ROOT),book);copies.saveAndFlush(copy);return response(copy);
 }
 public CopyResponse update(Long id,CopyRequest request){
  var copy=find(id);
  if(!copy.getBook().getId().equals(request.bookId()))throw new ResponseStatusException(HttpStatus.CONFLICT,"O livro de um exemplar não pode ser alterado.");
  copy.setInventoryCode(request.inventoryCode().strip().toUpperCase(Locale.ROOT));copies.flush();return response(copy);
 }
 public CopyResponse withdraw(Long id){var copy=find(id);copy.withdraw();copies.flush();return response(copy);}
 @Transactional(readOnly=true) public CopyResponse get(Long id){return response(find(id));}
 @Transactional(readOnly=true) public Page<CopyResponse> list(int page,int size){return copies.findAll(PageRequest.of(page,size,Sort.by("id"))).map(this::response);}
 public void delete(Long id){copies.delete(find(id));copies.flush();}
 private BookCopy find(Long id){return copies.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Exemplar não encontrado."));}
 private CopyResponse response(BookCopy copy){return new CopyResponse(copy.getId(),copy.getInventoryCode(),copy.getBook().getId(),copy.getCirculationStatus(),"IN_CIRCULATION".equals(copy.getCirculationStatus()));}
}
