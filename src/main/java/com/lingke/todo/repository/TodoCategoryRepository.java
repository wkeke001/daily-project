package com.lingke.todo.repository;

import com.lingke.todo.entity.TodoCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TodoCategoryRepository extends JpaRepository<TodoCategory, Long> {

    List<TodoCategory> findAllByOrderByCreatedAtAsc();

    List<TodoCategory> findAllByUserIdOrderByCreatedAtAsc(Long userId);

    Optional<TodoCategory> findByIdAndUserId(Long id, Long userId);
}
