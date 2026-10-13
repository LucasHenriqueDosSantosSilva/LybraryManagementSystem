package com.lucashenrique.library.api;

import java.security.Principal;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
  private final boolean enabled;

  public AuthController(@Value("${library.auth.enabled:true}") boolean enabled) {
    this.enabled = enabled;
  }

  @GetMapping("/api/auth/session")
  public Map<String, Object> session(Principal principal) {
    return Map.of(
        "authenticationRequired",
        enabled,
        "authenticated",
        principal != null,
        "username",
        principal == null ? "" : principal.getName());
  }

  @GetMapping("/api/auth/csrf")
  public Map<String, String> csrf(CsrfToken token) {
    return token == null
        ? Map.of()
        : Map.of("token", token.getToken(), "headerName", token.getHeaderName());
  }
}
