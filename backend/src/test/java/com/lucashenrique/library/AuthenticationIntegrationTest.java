package com.lucashenrique.library;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    properties = {"library.auth.enabled=true", "library.auth.password=Only-a-test-password-123"})
@AutoConfigureMockMvc
class AuthenticationIntegrationTest {
  @Autowired MockMvc mvc;

  @Test
  void protectsCatalogAndProvidesAnonymousSessionAndReadiness() throws Exception {
    mvc.perform(get("/api/books")).andExpect(status().isUnauthorized());
    mvc.perform(get("/api/auth/session")).andExpect(jsonPath("$.authenticated").value(false));
    mvc.perform(get("/api/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
  }

  @Test
  void rejectsLoginWithoutCsrfAndWithWrongPassword() throws Exception {
    mvc.perform(
            post("/api/auth/login").param("username", "bibliotecario").param("password", "wrong"))
        .andExpect(status().isForbidden());
    mvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .param("username", "bibliotecario")
                .param("password", "wrong"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void authenticatesEnforcesCsrfAndInvalidatesSessionAtLogout() throws Exception {
    var result =
        mvc.perform(
                post("/api/auth/login")
                    .with(csrf())
                    .param("username", "bibliotecario")
                    .param("password", "Only-a-test-password-123"))
            .andExpect(status().isNoContent())
            .andReturn();
    var session = (MockHttpSession) result.getRequest().getSession(false);
    mvc.perform(get("/api/books").session(session)).andExpect(status().isOk());
    mvc.perform(get("/api/auth/session").session(session))
        .andExpect(jsonPath("$.authenticated").value(true));
    mvc.perform(
            post("/api/categories")
                .session(session)
                .contentType("application/json")
                .content("{\"name\":\"Test\"}"))
        .andExpect(status().isForbidden());
    mvc.perform(post("/api/auth/logout").session(session).with(csrf()))
        .andExpect(status().isNoContent());
    org.junit.jupiter.api.Assertions.assertTrue(session.isInvalid());
    mvc.perform(get("/api/books")).andExpect(status().isUnauthorized());
  }
}
