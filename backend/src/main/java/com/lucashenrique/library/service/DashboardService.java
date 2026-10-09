package com.lucashenrique.library.service;

import com.lucashenrique.library.dto.DashboardResponse;
import com.lucashenrique.library.repository.DashboardRepository;
import java.time.Clock;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {
  private final DashboardRepository repository;
  private final Clock clock;

  public DashboardService(DashboardRepository repository, Clock clock) {
    this.repository = repository;
    this.clock = clock;
  }

  @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
  public DashboardResponse get(int limit) {
    LocalDate today = LocalDate.now(clock);
    return repository.fetch(today, limit);
  }
}
