package com.lucashenrique.library.catalog.repository;

import com.lucashenrique.library.catalog.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query("select e from Book e where e.id=:id")
  java.util.Optional<Book> locked(@org.springframework.data.repository.query.Param("id") Long id);

  @org.springframework.data.jpa.repository.Query(
      value =
          """
          SELECT b.* FROM books b
          WHERE (:title IS NULL OR INSTR(b.title,:title)>0)
          AND (:isbn IS NULL OR b.isbn=:isbn)
          AND (:categoryId IS NULL OR b.category_id=:categoryId)
          AND (:author IS NULL OR EXISTS (SELECT 1 FROM book_authors ba JOIN authors a ON a.id=ba.author_id WHERE ba.book_id=b.id AND INSTR(a.name,:author)>0))
          ORDER BY b.id
          """,
      countQuery =
          """
          SELECT COUNT(*) FROM books b
          WHERE (:title IS NULL OR INSTR(b.title,:title)>0)
          AND (:isbn IS NULL OR b.isbn=:isbn)
          AND (:categoryId IS NULL OR b.category_id=:categoryId)
          AND (:author IS NULL OR EXISTS (SELECT 1 FROM book_authors ba JOIN authors a ON a.id=ba.author_id WHERE ba.book_id=b.id AND INSTR(a.name,:author)>0))
          """,
      nativeQuery = true)
  org.springframework.data.domain.Page<Book> search(
      @org.springframework.data.repository.query.Param("title") String title,
      @org.springframework.data.repository.query.Param("isbn") String isbn,
      @org.springframework.data.repository.query.Param("categoryId") Long categoryId,
      @org.springframework.data.repository.query.Param("author") String author,
      org.springframework.data.domain.Pageable pageable);

  @org.springframework.data.jpa.repository.Query(
      "select distinct b from Book b left join fetch b.authors where b.id in :ids")
  java.util.List<Book> fetchAuthors(
      @org.springframework.data.repository.query.Param("ids") java.util.List<Long> ids);
}
