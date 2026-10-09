package com.lucashenrique.library.domain;

import java.time.LocalDate;
import java.util.Objects;

/** Classification at a reference date, rather than a persisted lifecycle state. */
public enum LoanStatus {
    ACTIVE,
    OVERDUE,
    RETURNED;

    public static LoanStatus classify(LocalDate dueDate, LocalDate returnedDate, LocalDate referenceDate) {
        Objects.requireNonNull(dueDate, "dueDate");
        Objects.requireNonNull(referenceDate, "referenceDate");
        if (returnedDate != null) {
            return RETURNED;
        }
        return dueDate.isBefore(referenceDate) ? OVERDUE : ACTIVE;
    }
}
