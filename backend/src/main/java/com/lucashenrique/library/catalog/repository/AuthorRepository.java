package com.lucashenrique.library.catalog.repository;

import com.lucashenrique.library.catalog.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, Long> {}
