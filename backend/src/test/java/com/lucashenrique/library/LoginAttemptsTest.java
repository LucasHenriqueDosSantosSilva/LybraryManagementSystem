package com.lucashenrique.library;

import static org.junit.jupiter.api.Assertions.*;

import com.lucashenrique.library.configuration.LoginAttempts;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class LoginAttemptsTest {
  @Test
  void blocksUntilBoundaryAndRoundsRetryUp() {
    AtomicLong now = new AtomicLong();
    LoginAttempts attempts = new LoginAttempts(2, 60, now::get);
    assertEquals(0, attempts.retryAfter());
    assertEquals(0, attempts.retryAfter());
    assertEquals(60, attempts.retryAfter());
    now.set(59_500_000_000L);
    assertEquals(1, attempts.retryAfter());
    now.set(60_000_000_000L);
    assertEquals(0, attempts.retryAfter());
  }

  @Test
  void admitsExactlyLimitUnderConcurrency() throws Exception {
    LoginAttempts attempts = new LoginAttempts(5, 60, () -> 0);
    ExecutorService pool = Executors.newFixedThreadPool(8);
    try {
      java.util.List<Callable<Long>> work =
          java.util.stream.IntStream.range(0, 20)
              .mapToObj(i -> (Callable<Long>) attempts::retryAfter)
              .toList();
      int admitted = 0;
      for (Future<Long> result : pool.invokeAll(work)) if (result.get() == 0) admitted++;
      assertEquals(5, admitted);
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void rejectsInvalidConfiguration() {
    assertThrows(IllegalArgumentException.class, () -> new LoginAttempts(0, 60, () -> 0));
  }
}
