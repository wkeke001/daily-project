package com.lingke.todo.service;

import com.lingke.todo.entity.Todo;
import com.lingke.todo.entity.TodoCategory;
import com.lingke.todo.repository.TodoCategoryRepository;
import com.lingke.todo.repository.TodoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TodoService {

    private final TodoRepository todoRepository;
    private final TodoCategoryRepository categoryRepository;

    public TodoService(TodoRepository todoRepository, TodoCategoryRepository categoryRepository) {
        this.todoRepository = todoRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Todo> findAll() {
        return todoRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Todo> findByDateAndCategory(LocalDate date, Long categoryId) {
        return todoRepository.findByDueDateAndCategoryIdOrderByCreatedAtAsc(date, categoryId);
    }

    public List<Todo> findByDate(LocalDate date) {
        return todoRepository.findByDueDateOrderByCreatedAtAsc(date);
    }

    public Todo add(String title, Long categoryId, LocalDate dueDate) {
        Todo todo = new Todo();
        todo.setTitle(title);
        todo.setDueDate(dueDate);
        if (categoryId != null) {
            categoryRepository.findById(categoryId).ifPresent(todo::setCategory);
        }
        return todoRepository.save(todo);
    }

    public void update(Long id, String title) {
        todoRepository.findById(id).ifPresent(todo -> {
            todo.setTitle(title);
            todoRepository.save(todo);
        });
    }

    public void toggleComplete(Long id) {
        todoRepository.findById(id).ifPresent(todo -> {
            todo.setCompleted(!todo.isCompleted());
            todoRepository.save(todo);
        });
    }

    public void delete(Long id) {
        todoRepository.deleteById(id);
    }

    public List<TodoCategory> findAllCategories() {
        return categoryRepository.findAllByOrderByCreatedAtAsc();
    }

    public TodoCategory addCategory(String name, String color) {
        TodoCategory category = new TodoCategory();
        category.setName(name);
        category.setColor(color);
        return categoryRepository.save(category);
    }

    public void updateCategory(Long id, String name, String color) {
        categoryRepository.findById(id).ifPresent(cat -> {
            cat.setName(name);
            if (color != null) cat.setColor(color);
            categoryRepository.save(cat);
        });
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}
