package com.lucashenrique.library.repository;
import com.lucashenrique.library.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CategoryRepository extends JpaRepository<Category, Long> {}
