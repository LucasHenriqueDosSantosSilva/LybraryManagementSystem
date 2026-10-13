package com.lucashenrique.library.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {
  @Bean
  UserDetailsService users(
      @Value("${library.auth.enabled:true}") boolean enabled,
      @Value("${library.auth.username:bibliotecario}") String username,
      @Value("${library.auth.password:}") String password) {
    if (!enabled) return new InMemoryUserDetailsManager();
    if (username.isBlank()
        || password.length() < 12
        || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
      throw new IllegalStateException(
          "Configure usuário e senha com ao menos 12 caracteres e até 72 bytes para autenticação.");
    return new InMemoryUserDetailsManager(
        User.withUsername(username)
            .password("{bcrypt}" + new BCryptPasswordEncoder().encode(password))
            .roles("LIBRARIAN")
            .build());
  }

  @Bean
  SecurityFilterChain security(
      HttpSecurity http, ObjectMapper json, @Value("${library.auth.enabled:true}") boolean enabled)
      throws Exception {
    if (!enabled) {
      http.csrf(csrf -> csrf.disable())
          .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
      return http.build();
    }
    http.authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/api/auth/session", "/api/auth/csrf", "/api/health", "/error")
                    .permitAll()
                    .anyRequest()
                    .hasRole("LIBRARIAN"))
        .formLogin(
            login ->
                login
                    .loginProcessingUrl("/api/auth/login")
                    .successHandler(
                        (request, response, authentication) -> {
                          response.setStatus(204);
                        })
                    .failureHandler(
                        (request, response, exception) ->
                            problem(
                                json,
                                response,
                                401,
                                "Credenciais inválidas.",
                                request.getRequestURI())))
        .logout(
            logout ->
                logout
                    .logoutUrl("/api/auth/logout")
                    .deleteCookies("JSESSIONID")
                    .logoutSuccessHandler(
                        (request, response, authentication) -> response.setStatus(204)))
        .exceptionHandling(
            errors ->
                errors
                    .authenticationEntryPoint(
                        (request, response, exception) ->
                            problem(
                                json,
                                response,
                                401,
                                "Entre para acessar a biblioteca.",
                                request.getRequestURI()))
                    .accessDeniedHandler(
                        (request, response, exception) ->
                            problem(
                                json,
                                response,
                                403,
                                "Acesso negado ou token CSRF inválido.",
                                request.getRequestURI())));
    return http.build();
  }

  private static void problem(
      ObjectMapper json, HttpServletResponse response, int status, String detail, String instance)
      throws IOException {
    response.setStatus(status);
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    json.writeValue(
        response.getOutputStream(),
        Map.of(
            "type",
            "about:blank",
            "status",
            status,
            "detail",
            detail,
            "title",
            org.springframework.http.HttpStatus.valueOf(status).getReasonPhrase(),
            "instance",
            instance));
  }
}
