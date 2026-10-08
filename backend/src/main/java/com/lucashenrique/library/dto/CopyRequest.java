package com.lucashenrique.library.dto;
import jakarta.validation.constraints.*;
public record CopyRequest(
 @NotBlank @Size(max=40) @Pattern(regexp=" *[A-Za-z0-9._-]+ *",message="Use letras ASCII, números, ponto, hífen ou sublinhado.") String inventoryCode,
 @NotNull @Positive Long bookId
) {}
