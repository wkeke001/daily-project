package com.lingke.todo.service;

import com.lingke.todo.entity.Todo;
import com.lingke.todo.entity.TodoCategory;
import com.lingke.todo.repository.TodoCategoryRepository;
import com.lingke.todo.repository.TodoRepository;
import com.lingke.todo.security.SecurityUtil;
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

    public List<Todo> findByDateAndCategory(LocalDate date, Long categoryId) {
        Long userId = SecurityUtil.getCurrentUserId();
        return todoRepository.findByDueDateAndCategoryIdAndUserIdOrderByCreatedAtAsc(date, categoryId, userId);
    }

    public Todo add(String title, String remark, Long categoryId, LocalDate dueDate) {
        Long userId = SecurityUtil.getCurrentUserId();
        Todo todo = new Todo();
        todo.setTitle(title);
        todo.setRemark(remark);
        todo.setDueDate(dueDate);
        todo.setUserId(userId);
        if (categoryId != null) {
            categoryRepository.findByIdAndUserId(categoryId, userId).ifPresent(todo::setCategory);
        }
        return todoRepository.save(todo);
    }

    public void update(Long id, String title, String remark) {
        Long userId = SecurityUtil.getCurrentUserId();
        todoRepository.findByIdAndUserId(id, userId).ifPresent(todo -> {
            todo.setTitle(title);
            todo.setRemark(remark);
            todoRepository.save(todo);
        });
    }

    public void toggleComplete(Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        todoRepository.findByIdAndUserId(id, userId).ifPresent(todo -> {
            todo.setCompleted(!todo.isCompleted());
            todoRepository.save(todo);
        });
    }

    public void delete(Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        todoRepository.findByIdAndUserId(id, userId).ifPresent(todo ->
            todoRepository.delete(todo)
        );
    }

    public List<TodoCategory> findAllCategories() {
        Long userId = SecurityUtil.getCurrentUserId();
        return categoryRepository.findAllByUserIdOrderByCreatedAtAsc(userId);
    }

    public TodoCategory addCategory(String name, String color) {
        Long userId = SecurityUtil.getCurrentUserId();
        TodoCategory category = new TodoCategory();
        category.setName(name);
        category.setColor(color);
        category.setUserId(userId);
        return categoryRepository.save(category);
    }

    public void updateCategory(Long id, String name, String color) {
        Long userId = SecurityUtil.getCurrentUserId();
        categoryRepository.findByIdAndUserId(id, userId).ifPresent(cat -> {
            cat.setName(name);
            if (color != null) cat.setColor(color);
            categoryRepository.save(cat);
        });
    }

    public void deleteCategory(Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        categoryRepository.findByIdAndUserId(id, userId).ifPresent(cat ->
            categoryRepository.delete(cat)
        );
    }
}
