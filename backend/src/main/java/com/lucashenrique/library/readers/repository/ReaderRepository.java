package com.lucashenrique.library.readers.repository;

import com.lucashenrique.library.readers.entity.Reader;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReaderRepository extends JpaRepository<Reader, Long> {
  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query("select e from Reader e where e.id=:id")
  java.util.Optional<Reader> locked(@org.springframework.data.repository.query.Param("id") Long id);
}
