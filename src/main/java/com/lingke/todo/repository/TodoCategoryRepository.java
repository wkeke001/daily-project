package com.lingke.todo.repository;

import com.lingke.todo.entity.TodoCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TodoCategoryRepository extends JpaRepository<TodoCategory, Long> {
    List<TodoCategory> findAllByOrderByCreatedAtAsc();
}
