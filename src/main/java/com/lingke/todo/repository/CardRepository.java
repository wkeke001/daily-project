package com.lingke.todo.repository;

import com.lingke.todo.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {

    @Query("SELECT DISTINCT c FROM Card c LEFT JOIN FETCH c.items ORDER BY c.createdAt DESC")
    List<Card> findAllWithItems();
}
