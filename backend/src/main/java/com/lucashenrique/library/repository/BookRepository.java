package com.lucashenrique.library.repository;
import com.lucashenrique.library.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
public interface BookRepository extends JpaRepository<Book,Long> {}
