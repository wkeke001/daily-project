package com.lingke.todo.repository;

import com.lingke.todo.entity.PasswordEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PasswordEntryRepository extends JpaRepository<PasswordEntry, Long> {
    List<PasswordEntry> findByUserIdOrderBySortOrderAsc(Long userId);
    List<PasswordEntry> findByCategoryIdAndUserIdOrderBySortOrderAsc(Long categoryId, Long userId);
    void deleteByCategoryIdAndUserId(Long categoryId, Long userId);

    @Query("SELECT e FROM PasswordEntry e WHERE e.userId = :userId AND (e.name LIKE %:keyword% OR e.account LIKE %:keyword% OR e.remark LIKE %:keyword%)")
    List<PasswordEntry> searchByKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);
}
