package com.lucashenrique.library.configuration;

import java.util.function.LongSupplier;

/** Fixed window for the single configured account, shared by all origins per process. */
public final class LoginAttempts {
  private final int limit;
  private final long window;
  private final LongSupplier time;
  private long start;
  private int count;

  public LoginAttempts(int limit, int seconds, LongSupplier time) {
    if (limit < 1 || limit > 100 || seconds < 1 || seconds > 3600)
      throw new IllegalArgumentException(
          "Limite de login: 1–100 tentativas, janela de 1–3600 segundos.");
    this.limit = limit;
    this.window = seconds * 1_000_000_000L;
    this.time = time;
  }

  public synchronized long retryAfter() {
    long now = time.getAsLong();
    if (count == 0 || now - start >= window) {
      start = now;
      count = 0;
    }
    if (count < limit) {
      count++;
      return 0;
    }
    return Math.max(1, (window - (now - start) + 999_999_999L) / 1_000_000_000L);
  }
}
