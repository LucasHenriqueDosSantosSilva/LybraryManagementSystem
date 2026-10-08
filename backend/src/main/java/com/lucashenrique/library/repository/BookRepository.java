package com.lucashenrique.library.repository;
import com.lucashenrique.library.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
public interface BookRepository extends JpaRepository<Book,Long> { @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
 @org.springframework.data.jpa.repository.Query("select e from Book e where e.id=:id")
 java.util.Optional<Book> locked(@org.springframework.data.repository.query.Param("id") Long id);
}
