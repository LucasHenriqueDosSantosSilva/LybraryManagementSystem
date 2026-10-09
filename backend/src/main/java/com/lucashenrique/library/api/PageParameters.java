package com.lucashenrique.library.api;

import com.lucashenrique.library.exception.InvalidInputException;

/** Validated pagination input shared by list endpoints. */
public record PageParameters(int page, int size) {
  public static final String DEFAULT_PAGE = "0";
  public static final String DEFAULT_SIZE = "20";
  public static final int MAX_SIZE = 100;

  public PageParameters {
    if (page < 0 || size < 1 || size > MAX_SIZE) {
      throw new InvalidInputException("Paginação inválida.");
    }
  }
}
