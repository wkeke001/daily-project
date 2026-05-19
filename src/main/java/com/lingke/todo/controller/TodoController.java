package com.lingke.todo.controller;

import com.lingke.todo.entity.TodoCategory;
import com.lingke.todo.service.TodoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping("/")
    public String index(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                        Model model) {
        LocalDate currentDate = (date != null) ? date : LocalDate.now();
        List<TodoCategory> categories = todoService.findAllCategories();

        Map<TodoCategory, List<com.lingke.todo.entity.Todo>> categoryTodos = new LinkedHashMap<>();
        for (TodoCategory cat : categories) {
            categoryTodos.put(cat, todoService.findByDateAndCategory(currentDate, cat.getId()));
        }

        model.addAttribute("currentDate", currentDate);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("prevDate", currentDate.minusDays(1));
        model.addAttribute("nextDate", currentDate.plusDays(1));
        model.addAttribute("categories", categories);
        model.addAttribute("categoryTodos", categoryTodos);
        return "index";
    }

    @PostMapping("/add")
    public String add(@RequestParam String title,
                      @RequestParam(required = false) String remark,
                      @RequestParam Long categoryId,
                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate) {
        todoService.add(title, remark, categoryId, dueDate);
        return "redirect:/?date=" + dueDate;
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam String title,
                         @RequestParam(required = false) String remark,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        todoService.update(id, title, remark);
        return "redirect:/?date=" + date;
    }

    @PostMapping("/toggle/{id}")
    public String toggle(@PathVariable Long id,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        todoService.toggleComplete(id);
        LocalDate redirectDate = (date != null) ? date : LocalDate.now();
        return "redirect:/?date=" + redirectDate;
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        todoService.delete(id);
        LocalDate redirectDate = (date != null) ? date : LocalDate.now();
        return "redirect:/?date=" + redirectDate;
    }

    @PostMapping("/categories/add")
    public String addCategory(@RequestParam String name,
                              @RequestParam(required = false) String color,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        todoService.addCategory(name, color);
        LocalDate redirectDate = (date != null) ? date : LocalDate.now();
        return "redirect:/?date=" + redirectDate;
    }

    @PostMapping("/categories/update/{id}")
    public String updateCategory(@PathVariable Long id,
                                 @RequestParam String name,
                                 @RequestParam(required = false) String color,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        todoService.updateCategory(id, name, color);
        LocalDate redirectDate = (date != null) ? date : LocalDate.now();
        return "redirect:/?date=" + redirectDate;
    }

    @PostMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        todoService.deleteCategory(id);
        LocalDate redirectDate = (date != null) ? date : LocalDate.now();
        return "redirect:/?date=" + redirectDate;
    }
}
