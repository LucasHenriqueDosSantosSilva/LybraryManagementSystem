package com.lucashenrique.library.api;

import com.lucashenrique.library.exception.BusinessConflictException;
import com.lucashenrique.library.exception.InvalidInputException;
import com.lucashenrique.library.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ApiExceptionHandlerTest {
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new ErrorController())
            .setControllerAdvice(new ApiExceptionHandler()).build();

    @RestController
    static class ErrorController {
        @GetMapping("/missing")
        void missing() { throw new ResourceNotFoundException("Recurso não encontrado."); }
        @GetMapping("/conflict")
        void conflict() { throw new BusinessConflictException("Operação incompatível."); }
        @GetMapping("/invalid")
        void invalid() { throw new InvalidInputException("Entrada inválida."); }
    }

    @Test void translatesMissingResource() throws Exception {
        mvc.perform(get("/missing")).andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(404)).andExpect(jsonPath("$.detail").value("Recurso não encontrado."));
    }
    @Test void translatesBusinessConflict() throws Exception {
        mvc.perform(get("/conflict")).andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409)).andExpect(jsonPath("$.detail").value("Operação incompatível."));
    }
    @Test void translatesInvalidInput() throws Exception {
        mvc.perform(get("/invalid")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400)).andExpect(jsonPath("$.detail").value("Entrada inválida."));
    }
}
