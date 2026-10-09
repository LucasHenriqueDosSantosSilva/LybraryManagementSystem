package com.lucashenrique.library.catalog.domain;

import java.util.regex.Pattern;

public final class Isbn {
  private static final Pattern SEPARATORS =
      Pattern.compile("[-\\s]", Pattern.UNICODE_CHARACTER_CLASS);

  private Isbn() {}

  public static String canonicalize(String input) {
    if (input == null) throw new IllegalArgumentException("Informe um ISBN válido.");
    String value = SEPARATORS.matcher(input).replaceAll("");
    if (value.matches("[0-9]{9}[0-9Xx]")) {
      int sum = 0;
      for (int i = 0; i < 10; i++)
        sum +=
            (10 - i)
                * (i == 9 && Character.toUpperCase(value.charAt(i)) == 'X'
                    ? 10
                    : value.charAt(i) - '0');
      if (sum % 11 != 0) throw new IllegalArgumentException("Dígito verificador do ISBN inválido.");
      String prefix = "978" + value.substring(0, 9);
      return prefix + checkDigit(prefix);
    }
    if (value.matches("(?:978|979)[0-9]{10}")
        && value.charAt(12) - '0' == checkDigit(value.substring(0, 12))) return value;
    throw new IllegalArgumentException("Informe um ISBN-10 ou ISBN-13 válido.");
  }

  private static int checkDigit(String prefix) {
    int sum = 0;
    for (int i = 0; i < 12; i++) sum += (prefix.charAt(i) - '0') * (i % 2 == 0 ? 1 : 3);
    return (10 - sum % 10) % 10;
  }
}
