package com.lingke.todo.controller;

import com.lingke.todo.entity.CategoryTemplate;
import com.lingke.todo.entity.TodoCategory;
import com.lingke.todo.repository.CategoryTemplateRepository;
import com.lingke.todo.security.SecurityUtil;
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
    private final CategoryTemplateRepository templateRepository;

    public TodoController(TodoService todoService, CategoryTemplateRepository templateRepository) {
        this.todoService = todoService;
        this.templateRepository = templateRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        LocalDate today = LocalDate.now();
        List<TodoCategory> categories = todoService.findAllCategories();
        Map<LocalDate, List<com.lingke.todo.entity.Todo>> dateTodos = todoService.findAllGroupedByDate();

        // 今天：显示所有分类（即使为空），方便添加
        Map<TodoCategory, List<com.lingke.todo.entity.Todo>> todayCatMap = new LinkedHashMap<>();
        List<com.lingke.todo.entity.Todo> todayTodos = dateTodos.getOrDefault(today, List.of());
        for (TodoCategory cat : categories) {
            List<com.lingke.todo.entity.Todo> filtered = todayTodos.stream()
                    .filter(t -> t.getCategory() != null && t.getCategory().getId().equals(cat.getId()))
                    .collect(java.util.stream.Collectors.toList());
            todayCatMap.put(cat, filtered);
        }

        // 历史日期：只显示有内容的分类
        Map<LocalDate, Map<TodoCategory, List<com.lingke.todo.entity.Todo>>> dateGrouped = new LinkedHashMap<>();
        for (Map.Entry<LocalDate, List<com.lingke.todo.entity.Todo>> entry : dateTodos.entrySet()) {
            if (entry.getKey().equals(today)) continue;
            Map<TodoCategory, List<com.lingke.todo.entity.Todo>> catMap = new LinkedHashMap<>();
            for (TodoCategory cat : categories) {
                List<com.lingke.todo.entity.Todo> filtered = entry.getValue().stream()
                        .filter(t -> t.getCategory() != null && t.getCategory().getId().equals(cat.getId()))
                        .collect(java.util.stream.Collectors.toList());
                if (!filtered.isEmpty()) {
                    catMap.put(cat, filtered);
                }
            }
            if (!catMap.isEmpty()) {
                dateGrouped.put(entry.getKey(), catMap);
            }
        }

        model.addAttribute("today", today);
        model.addAttribute("categories", categories);
        model.addAttribute("todayCatMap", todayCatMap);
        model.addAttribute("dateGrouped", dateGrouped);
        model.addAttribute("categoryTemplates", templateRepository.findAllByUserIdOrderByCreatedAtAsc(SecurityUtil.getCurrentUserId()));
        return "index";
    }

    @PostMapping("/add")
    public String add(@RequestParam String title,
                      @RequestParam(required = false) String remark,
                      @RequestParam Long categoryId,
                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate) {
        todoService.add(title, remark, categoryId, dueDate);
        return "redirect:/";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam String title,
                         @RequestParam(required = false) String remark,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        todoService.update(id, title, remark);
        return "redirect:/";
    }

    @PostMapping("/toggle/{id}")
    public String toggle(@PathVariable Long id,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        todoService.toggleComplete(id);
        return "redirect:/";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        todoService.delete(id);
        return "redirect:/";
    }

    @PostMapping("/categories/add")
    public String addCategory(@RequestParam String name,
                              @RequestParam(required = false) String color,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long userId = SecurityUtil.getCurrentUserId();
        todoService.addCategory(name, color);
        if (!templateRepository.existsByNameAndUserId(name, userId)) {
            CategoryTemplate tpl = new CategoryTemplate();
            tpl.setName(name);
            tpl.setUserId(userId);
            templateRepository.save(tpl);
        }
        return "redirect:/";
    }

    @PostMapping("/categories/update/{id}")
    public String updateCategory(@PathVariable Long id,
                                 @RequestParam String name,
                                 @RequestParam(required = false) String color,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        todoService.updateCategory(id, name, color);
        return "redirect:/";
    }

    @PostMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        todoService.deleteCategory(id);
        return "redirect:/";
    }

    @PostMapping("/categories/templates/add")
    public String addTemplate(@RequestParam String name) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (!templateRepository.existsByNameAndUserId(name, userId)) {
            CategoryTemplate tpl = new CategoryTemplate();
            tpl.setName(name);
            tpl.setUserId(userId);
            templateRepository.save(tpl);
        }
        return "redirect:/";
    }

    @PostMapping("/categories/templates/delete/{id}")
    public String deleteTemplate(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        templateRepository.findByIdAndUserId(id, userId).ifPresent(templateRepository::delete);
        return "redirect:/";
    }
}
