package com.lucashenrique.library.service;
import com.lucashenrique.library.exception.BusinessConflictException;
import com.lucashenrique.library.exception.ResourceNotFoundException;
import com.lucashenrique.library.dto.*;
import com.lucashenrique.library.entity.Reader;
import com.lucashenrique.library.repository.ReaderRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;
@Service @Transactional(isolation=org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
public class ReaderService {
 private final ReaderRepository readers;private final com.lucashenrique.library.repository.LoanRepository loans;
 public ReaderService(ReaderRepository readers,com.lucashenrique.library.repository.LoanRepository loans){this.readers=readers;this.loans=loans;}
 public ReaderResponse create(ReaderRequest request){Reader reader=new Reader();apply(reader,request);readers.saveAndFlush(reader);return response(reader);}
 public ReaderResponse update(Long id,ReaderRequest request){Reader reader=find(id);apply(reader,request);readers.flush();return response(reader);}
 public ReaderResponse status(Long id,boolean active){Reader reader=find(id);reader.setActive(active);readers.flush();return response(reader);}
 @Transactional(readOnly=true) public ReaderResponse get(Long id){return response(readers.findById(id).orElseThrow(()->new ResourceNotFoundException("Leitor não encontrado.")));}
 @Transactional(readOnly=true) public Page<ReaderResponse> list(int page,int size){return readers.findAll(PageRequest.of(page,size,Sort.by("id"))).map(this::response);}
 public void delete(Long id){var reader=find(id);if(loans.existsByReaderId(id))throw new BusinessConflictException("Leitor com histórico deve ser desativado.");readers.delete(reader);readers.flush();}
 private Reader find(Long id){return readers.locked(id).orElseThrow(()->new ResourceNotFoundException("Leitor não encontrado."));}
 private void apply(Reader reader,ReaderRequest request){
  String email=request.email()==null?null:request.email().strip();
  if(email!=null && email.isEmpty())email=null;
  reader.setDetails(request.registrationNumber().strip().toUpperCase(Locale.ROOT),request.name().strip(),email);
 }
 private ReaderResponse response(Reader reader){return new ReaderResponse(reader.getId(),reader.getRegistrationNumber(),reader.getName(),reader.getEmail(),reader.isActive());}
}
