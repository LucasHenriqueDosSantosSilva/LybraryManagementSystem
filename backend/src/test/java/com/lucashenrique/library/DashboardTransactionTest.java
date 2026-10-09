package com.lucashenrique.library;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;

import com.lucashenrique.library.repository.DashboardRepository;
import com.lucashenrique.library.service.DashboardService;
import java.sql.Connection;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@SpringBootTest
class DashboardTransactionTest {
  @Autowired DashboardService service;
  @MockitoSpyBean DashboardRepository repository;

  @Test
  void repositoryUsesServiceReadOnlyRepeatableReadTransaction() {
    DashboardRepository target =
        org.springframework.test.util.AopTestUtils.getUltimateTargetObject(repository);
    doAnswer(
            invocation -> {
              assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
              assertTrue(TransactionSynchronizationManager.isCurrentTransactionReadOnly());
              assertEquals(
                  Connection.TRANSACTION_REPEATABLE_READ,
                  TransactionSynchronizationManager.getCurrentTransactionIsolationLevel());
              return invocation.callRealMethod();
            })
        .when(target)
        .fetch(any(LocalDate.class), eq(5));
    assertNotNull(service.get(5));
  }

  @Test
  void directRepositoryCallRequiresTransaction() {
    assertThrows(
        IllegalTransactionStateException.class,
        () -> repository.fetch(LocalDate.of(2026, 10, 22), 5));
  }
}
