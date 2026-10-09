package com.lucashenrique.library;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PaginationContractTest {
  @Autowired MockMvc mvc;

  @ParameterizedTest
  @ValueSource(strings = {"categories", "authors", "books", "readers", "copies", "loans"})
  void preservesDefaultsLimitsAndErrorsAcrossEndpoints(String resource) throws Exception {
    String path = "/api/" + resource;
    mvc.perform(get(path))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.page.number").value(0))
        .andExpect(jsonPath("$.page.size").value(20));
    for (int size : new int[] {1, 100}) {
      mvc.perform(get(path).param("size", Integer.toString(size)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.page.size").value(size));
    }
    for (int size : new int[] {0, 101}) {
      mvc.perform(get(path).param("size", Integer.toString(size)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.detail").value("Paginação inválida."));
    }
    mvc.perform(get(path).param("page", "-1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Paginação inválida."));
    mvc.perform(get(path).param("page", "not-a-number")).andExpect(status().isBadRequest());
  }
}
