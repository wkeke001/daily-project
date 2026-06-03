package com.lingke.todo.repository;

import com.lingke.todo.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    List<Todo> findAllByOrderByCreatedAtDesc();

    List<Todo> findByDueDateAndCategoryIdOrderByCreatedAtAsc(LocalDate dueDate, Long categoryId);

    List<Todo> findByDueDateOrderByCreatedAtAsc(LocalDate dueDate);

    List<Todo> findByDueDateAndCategoryIdAndUserIdOrderByCreatedAtAsc(LocalDate dueDate, Long categoryId, Long userId);

    Optional<Todo> findByIdAndUserId(Long id, Long userId);

    List<Todo> findByUserIdOrderByDueDateDescCreatedAtAsc(Long userId);
}
