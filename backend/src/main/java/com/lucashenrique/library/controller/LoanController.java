package com.lucashenrique.library.controller;
import com.lucashenrique.library.dto.*;
import com.lucashenrique.library.service.LoanService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.data.domain.Page;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/loans")
public class LoanController {
 private final LoanService service;public LoanController(LoanService service){this.service=service;}
 @PostMapping public ResponseEntity<LoanResponse> create(@Valid @RequestBody LoanRequest request){var result=service.create(request);return ResponseEntity.created(URI.create("/api/loans/"+result.id())).body(result);}
 @PostMapping("/{id}/return") public LoanResponse returnLoan(@PathVariable Long id){return service.returnLoan(id);}
 @GetMapping("/{id}") public LoanResponse get(@PathVariable Long id){return service.get(id);}
 @GetMapping public Page<LoanResponse> list(@RequestParam(required=false) Long readerId,@RequestParam(required=false) String status,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){
  if(page<0||size<1||size>100)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Paginação inválida.");
  return service.list(readerId,status,page,size);
 }
}
