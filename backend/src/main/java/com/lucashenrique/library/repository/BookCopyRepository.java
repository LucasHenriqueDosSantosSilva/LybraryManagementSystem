package com.lucashenrique.library.repository;
import com.lucashenrique.library.entity.BookCopy;
import org.springframework.data.jpa.repository.JpaRepository;
public interface BookCopyRepository extends JpaRepository<BookCopy,Long> {}
