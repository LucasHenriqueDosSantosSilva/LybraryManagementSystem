package com.lucashenrique.library.reporting.repository;

import com.lucashenrique.library.loans.domain.LoanStatus;
import com.lucashenrique.library.loans.dto.LoanResponse;
import com.lucashenrique.library.reporting.dto.DashboardResponse;
import java.time.LocalDate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class DashboardRepository {
  private final JdbcTemplate jdbc;

  public DashboardRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public DashboardResponse fetch(LocalDate today, int limit) {
    long books = jdbc.queryForObject("SELECT COUNT(*) FROM books", Long.class);
    long copies = jdbc.queryForObject("SELECT COUNT(*) FROM book_copies", Long.class);
    long readers = jdbc.queryForObject("SELECT COUNT(*) FROM readers", Long.class);
    long available =
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM book_copies c WHERE c.circulation_status='IN_CIRCULATION' AND NOT"
                + " EXISTS (SELECT 1 FROM loans l WHERE l.copy_id=c.id AND l.returned_date IS"
                + " NULL)",
            Long.class);
    long active =
        jdbc.queryForObject("SELECT COUNT(*) FROM loans WHERE returned_date IS NULL", Long.class);
    long overdue =
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM loans WHERE returned_date IS NULL AND due_date<?",
            Long.class,
            today);
    var popular =
        jdbc.query(
            "SELECT b.id,b.title,COUNT(*) AS loan_count FROM loans l JOIN book_copies c ON"
                + " c.id=l.copy_id JOIN books b ON b.id=c.book_id GROUP BY b.id,b.title ORDER BY"
                + " loan_count DESC,b.id ASC LIMIT ?",
            (rs, row) ->
                new DashboardResponse.PopularBook(
                    rs.getLong("id"), rs.getString("title"), rs.getLong("loan_count")),
            limit);
    var recent =
        jdbc.query(
            "SELECT l.*,c.book_id FROM loans l JOIN book_copies c ON c.id=l.copy_id ORDER BY"
                + " l.loan_date DESC,l.id DESC LIMIT ?",
            (rs, row) -> {
              LocalDate returned = rs.getObject("returned_date", LocalDate.class),
                  due = rs.getObject("due_date", LocalDate.class);
              return new LoanResponse(
                  rs.getLong("id"),
                  rs.getLong("reader_id"),
                  rs.getLong("copy_id"),
                  rs.getLong("book_id"),
                  rs.getObject("loan_date", LocalDate.class),
                  due,
                  returned,
                  LoanStatus.classify(due, returned, today).name());
            },
            limit);
    return new DashboardResponse(
        books, copies, readers, available, active, overdue, popular, recent);
  }
}
