package com.lucashenrique.library.catalog.repository;

import com.lucashenrique.library.catalog.entity.BookCopy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {
  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query("select e from BookCopy e where e.id=:id")
  java.util.Optional<BookCopy> locked(
      @org.springframework.data.repository.query.Param("id") Long id);

  @org.springframework.data.jpa.repository.Query(
      value = "SELECT book_id FROM book_copies WHERE id=:id",
      nativeQuery = true)
  java.util.Optional<Long> bookId(@org.springframework.data.repository.query.Param("id") Long id);
}
