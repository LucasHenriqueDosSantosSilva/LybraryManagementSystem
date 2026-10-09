package com.lucashenrique.library;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RestContractTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;

  @ParameterizedTest
  @ValueSource(strings = {"categories", "authors", "books", "readers", "copies", "loans"})
  void rejectsMalformedIdentifiersWithProblemDetail(String resource) throws Exception {
    mvc.perform(get("/api/" + resource + "/invalid"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.instance").value("/api/" + resource + "/invalid"));
  }

  @Test void malformedJsonHasProblemDetail() throws Exception {
    mvc.perform(post("/api/categories").contentType("application/json").content("{"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
        .andExpect(jsonPath("$.status").value(400));
  }

  @Test void invalidFieldsAreNamed() throws Exception {
    mvc.perform(post("/api/categories").contentType("application/json").content("{\"name\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
        .andExpect(jsonPath("$.errors[0].field").value("name"));
  }

  @Test void unsupportedMethodAdvertisesAllowedMethods() throws Exception {
    mvc.perform(patch("/api/categories"))
        .andExpect(status().isMethodNotAllowed())
        .andExpect(header().exists("Allow"))
        .andExpect(content().contentTypeCompatibleWith("application/problem+json"));
  }

  @Test void createdLocationRetrievesResourceAndDeletionHasNoBody() throws Exception {
    String body = json.createObjectNode().put("name", "REST-" + UUID.randomUUID()).toString();
    var response = mvc.perform(post("/api/categories").contentType("application/json").content(body))
        .andExpect(status().isCreated()).andExpect(header().exists("Location"))
        .andReturn().getResponse();
    long id = json.readTree(response.getContentAsString()).get("id").asLong();
    String path = "/api/categories/" + id;
    try {
      org.junit.jupiter.api.Assertions.assertEquals(path, response.getHeader("Location"));
      mvc.perform(get(path)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
    } finally {
      mvc.perform(delete(path)).andExpect(status().isNoContent()).andExpect(content().string(""));
    }
    mvc.perform(get(path)).andExpect(status().isNotFound())
        .andExpect(content().contentTypeCompatibleWith("application/problem+json"));
  }
}
