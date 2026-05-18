package com.lingke.todo;

import com.lingke.todo.entity.Todo;
import com.lingke.todo.repository.TodoRepository;
import com.lingke.todo.service.TodoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TodoServiceTest {

    @Autowired
    private TodoService todoService;

    @Autowired
    private TodoRepository todoRepository;

    @BeforeEach
    void setUp() {
        todoRepository.deleteAll();
    }

    @Test
    void shouldAddTodo() {
        Todo todo = todoService.add("Learn Spring Boot", null, LocalDate.now());
        assertNotNull(todo.getId());
        assertEquals("Learn Spring Boot", todo.getTitle());
        assertFalse(todo.isCompleted());
    }

    @Test
    void shouldFindAllOrderByCreatedAtDesc() {
        todoService.add("First", null, LocalDate.now());
        todoService.add("Second", null, LocalDate.now());
        List<Todo> todos = todoService.findAll();
        assertEquals(2, todos.size());
        assertEquals("Second", todos.get(0).getTitle());
    }

    @Test
    void shouldToggleComplete() {
        Todo todo = todoService.add("Test toggle", null, LocalDate.now());
        assertFalse(todo.isCompleted());

        todoService.toggleComplete(todo.getId());
        Todo updated = todoRepository.findById(todo.getId()).orElseThrow();
        assertTrue(updated.isCompleted());
    }

    @Test
    void shouldDelete() {
        Todo todo = todoService.add("To be deleted", null, LocalDate.now());
        todoService.delete(todo.getId());
        assertTrue(todoRepository.findById(todo.getId()).isEmpty());
    }
}
