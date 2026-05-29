package com.lingke.todo.repository;

import com.lingke.todo.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUserIdAndNoteDateOrderByCreatedAtDesc(Long userId, LocalDate noteDate);
    List<Note> findByUserIdOrderByNoteDateDescCreatedAtDesc(Long userId);
}
