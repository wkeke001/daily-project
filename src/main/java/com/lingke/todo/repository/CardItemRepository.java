package com.lingke.todo.repository;

import com.lingke.todo.entity.CardItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardItemRepository extends JpaRepository<CardItem, Long> {
}
