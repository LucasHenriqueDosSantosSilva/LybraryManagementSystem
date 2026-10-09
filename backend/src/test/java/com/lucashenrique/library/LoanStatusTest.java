package com.lucashenrique.library;

import com.lucashenrique.library.domain.LoanStatus;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LoanStatusTest {
    private final LocalDate dueDate = LocalDate.of(2026, 10, 22);

    @Test void activeBeforeDueDate() {
        assertEquals(LoanStatus.ACTIVE, LoanStatus.classify(dueDate, null, dueDate.minusDays(1)));
    }
    @Test void activeOnDueDate() {
        assertEquals(LoanStatus.ACTIVE, LoanStatus.classify(dueDate, null, dueDate));
    }
    @Test void overdueAfterDueDate() {
        assertEquals(LoanStatus.OVERDUE, LoanStatus.classify(dueDate, null, dueDate.plusDays(1)));
    }
    @Test void returnedOnTimeStaysReturned() {
        assertEquals(LoanStatus.RETURNED, LoanStatus.classify(dueDate, dueDate, dueDate.plusDays(30)));
    }
    @Test void lateReturnIsNotCurrentlyOverdue() {
        assertEquals(LoanStatus.RETURNED, LoanStatus.classify(dueDate, dueDate.plusDays(2), dueDate.plusDays(30)));
    }
}
