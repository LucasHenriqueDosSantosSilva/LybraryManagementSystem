package com.lucashenrique.library.service;
import com.lucashenrique.library.dto.*;
import com.lucashenrique.library.entity.Reader;
import com.lucashenrique.library.repository.ReaderRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.Locale;
@Service @Transactional
public class ReaderService {
 private final ReaderRepository readers;
 public ReaderService(ReaderRepository readers){this.readers=readers;}
 public ReaderResponse create(ReaderRequest request){Reader reader=new Reader();apply(reader,request);readers.saveAndFlush(reader);return response(reader);}
 public ReaderResponse update(Long id,ReaderRequest request){Reader reader=find(id);apply(reader,request);readers.flush();return response(reader);}
 public ReaderResponse status(Long id,boolean active){Reader reader=find(id);reader.setActive(active);readers.flush();return response(reader);}
 @Transactional(readOnly=true) public ReaderResponse get(Long id){return response(find(id));}
 @Transactional(readOnly=true) public Page<ReaderResponse> list(int page,int size){return readers.findAll(PageRequest.of(page,size,Sort.by("id"))).map(this::response);}
 public void delete(Long id){readers.delete(find(id));readers.flush();}
 private Reader find(Long id){return readers.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Leitor não encontrado."));}
 private void apply(Reader reader,ReaderRequest request){
  String email=request.email()==null?null:request.email().strip();
  if(email!=null && email.isEmpty())email=null;
  reader.setDetails(request.registrationNumber().strip().toUpperCase(Locale.ROOT),request.name().strip(),email);
 }
 private ReaderResponse response(Reader reader){return new ReaderResponse(reader.getId(),reader.getRegistrationNumber(),reader.getName(),reader.getEmail(),reader.isActive());}
}
