package com.lucashenrique.library.controller;
import com.lucashenrique.library.dto.DashboardResponse;
import com.lucashenrique.library.service.DashboardService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/dashboard")
public class DashboardController {
 private final DashboardService service;public DashboardController(DashboardService service){this.service=service;}
 @GetMapping public DashboardResponse get(@RequestParam(defaultValue="5") int limit){
  if(limit<1||limit>20)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Limite deve ser de 1 a 20.");
  return service.get(limit);
 }
}
