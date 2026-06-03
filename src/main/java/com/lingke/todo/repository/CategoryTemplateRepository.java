package com.lingke.todo.repository;

import com.lingke.todo.entity.CategoryTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryTemplateRepository extends JpaRepository<CategoryTemplate, Long> {

    List<CategoryTemplate> findAllByUserIdOrderByCreatedAtAsc(Long userId);

    Optional<CategoryTemplate> findByIdAndUserId(Long id, Long userId);

    boolean existsByNameAndUserId(String name, Long userId);
}
