package com.lucashenrique.library.repository;
import com.lucashenrique.library.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AuthorRepository extends JpaRepository<Author, Long> {}
