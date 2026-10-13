package com.lucashenrique.library.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.web.filter.OncePerRequestFilter;

public final class LoginThrottleFilter extends OncePerRequestFilter {
  private final LoginAttempts attempts;
  private final ObjectMapper json;

  public LoginThrottleFilter(LoginAttempts attempts, ObjectMapper json) {
    this.attempts = attempts;
    this.json = json;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    if ("POST".equals(request.getMethod())
        && (request.getContextPath() + "/api/auth/login").equals(request.getRequestURI())) {
      long wait = attempts.retryAfter();
      if (wait > 0) {
        response.setStatus(429);
        response.setHeader("Retry-After", Long.toString(wait));
        response.setContentType("application/problem+json");
        json.writeValue(
            response.getOutputStream(),
            Map.of(
                "type",
                "about:blank",
                "title",
                "Too Many Requests",
                "status",
                429,
                "detail",
                "Muitas tentativas de login. Aguarde e tente novamente.",
                "instance",
                request.getRequestURI()));
        return;
      }
    }
    chain.doFilter(request, response);
  }
}
