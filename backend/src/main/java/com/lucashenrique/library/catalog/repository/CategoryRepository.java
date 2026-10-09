package com.lucashenrique.library.catalog.repository;

import com.lucashenrique.library.catalog.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {}
