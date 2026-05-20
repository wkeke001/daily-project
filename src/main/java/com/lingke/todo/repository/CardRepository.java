package com.lingke.todo.repository;

import com.lingke.todo.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {

    @Query("SELECT DISTINCT c FROM Card c LEFT JOIN FETCH c.items ORDER BY c.createdAt DESC")
    List<Card> findAllWithItems();

    @Query("SELECT DISTINCT c FROM Card c LEFT JOIN FETCH c.items WHERE c.userId = :userId ORDER BY c.createdAt DESC")
    List<Card> findAllWithItemsByUserId(@Param("userId") Long userId);

    Optional<Card> findByIdAndUserId(Long id, Long userId);
}
