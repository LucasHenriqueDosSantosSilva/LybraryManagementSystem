package com.lucashenrique.library;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    properties = {
      "library.auth.enabled=true",
      "library.auth.password=Only-a-test-password-123",
      "library.auth.login-limit=2"
    })
@AutoConfigureMockMvc
class LoginThrottleIntegrationTest {
  @Autowired MockMvc mvc;

  @Test
  void limitsBeforePasswordCheckAndIgnoresSpoofedForwarding() throws Exception {
    for (int i = 0; i < 2; i++)
      mvc.perform(
              post("/api/auth/login")
                  .with(csrf())
                  .param("username", "unknown" + i)
                  .param("password", "wrong"))
          .andExpect(status().isUnauthorized());
    mvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .header("X-Forwarded-For", "198.51.100.1")
                .param("username", "bibliotecario")
                .param("password", "Only-a-test-password-123"))
        .andExpect(status().isTooManyRequests())
        .andExpect(header().exists("Retry-After"))
        .andExpect(jsonPath("$.status").value(429));
  }
}
