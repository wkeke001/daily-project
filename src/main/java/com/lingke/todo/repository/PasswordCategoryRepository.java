package com.lingke.todo.repository;

import com.lingke.todo.entity.PasswordCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PasswordCategoryRepository extends JpaRepository<PasswordCategory, Long> {
    List<PasswordCategory> findByUserIdOrderBySortOrderAsc(Long userId);
    void deleteByIdAndUserId(Long id, Long userId);
}
