package com.lingke.todo.controller;

import com.lingke.todo.entity.PasswordCategory;
import com.lingke.todo.entity.PasswordEntry;
import com.lingke.todo.repository.PasswordCategoryRepository;
import com.lingke.todo.repository.PasswordEntryRepository;
import com.lingke.todo.security.SecurityUtil;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/passwords")
public class PasswordController {

    private final PasswordCategoryRepository categoryRepository;
    private final PasswordEntryRepository entryRepository;

    public PasswordController(PasswordCategoryRepository categoryRepository,
                              PasswordEntryRepository entryRepository) {
        this.categoryRepository = categoryRepository;
        this.entryRepository = entryRepository;
    }

    @GetMapping
    public String index(Model model) {
        Long userId = SecurityUtil.getCurrentUserId();
        List<PasswordCategory> categories = categoryRepository.findByUserIdOrderBySortOrderAsc(userId);
        List<PasswordEntry> entries = entryRepository.findByUserIdOrderBySortOrderAsc(userId);

        Map<Long, List<PasswordEntry>> grouped = entries.stream()
                .collect(Collectors.groupingBy(PasswordEntry::getCategoryId, LinkedHashMap::new, Collectors.toList()));

        model.addAttribute("categories", categories);
        model.addAttribute("grouped", grouped);
        return "passwords";
    }

    @PostMapping("/import")
    @ResponseBody
    @Transactional
    public Map<String, Object> importData(@RequestBody List<Map<String, Object>> data) {
        Long userId = SecurityUtil.getCurrentUserId();
        int order = 0;
        for (Map<String, Object> group : data) {
            String categoryName = (String) group.get("category");
            PasswordCategory category = new PasswordCategory();
            category.setName(categoryName);
            category.setSortOrder(order++);
            category.setUserId(userId);
            categoryRepository.save(category);

            @SuppressWarnings("unchecked")
            List<Map<String, String>> entriesList = (List<Map<String, String>>) group.get("entries");
            int entryOrder = 0;
            for (Map<String, String> item : entriesList) {
                PasswordEntry entry = new PasswordEntry();
                entry.setCategoryId(category.getId());
                entry.setName(item.getOrDefault("name", ""));
                entry.setAccount(item.getOrDefault("account", ""));
                entry.setPassword(item.getOrDefault("password", ""));
                entry.setRemark(item.getOrDefault("remark", ""));
                entry.setSortOrder(entryOrder++);
                entry.setUserId(userId);
                entryRepository.save(entry);
            }
        }
        return Map.of("success", true);
    }

    @PostMapping("/category/add")
    public String addCategory(@RequestParam String name) {
        Long userId = SecurityUtil.getCurrentUserId();
        PasswordCategory category = new PasswordCategory();
        category.setName(name);
        category.setUserId(userId);
        categoryRepository.save(category);
        return "redirect:/passwords";
    }

    @PostMapping("/category/update/{id}")
    public String updateCategory(@PathVariable Long id, @RequestParam String name) {
        Long userId = SecurityUtil.getCurrentUserId();
        PasswordCategory category = categoryRepository.findById(id).orElseThrow();
        if (!category.getUserId().equals(userId)) throw new IllegalStateException("No permission");
        category.setName(name);
        categoryRepository.save(category);
        return "redirect:/passwords";
    }

    @PostMapping("/category/delete/{id}")
    @Transactional
    public String deleteCategory(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        PasswordCategory category = categoryRepository.findById(id).orElseThrow();
        if (!category.getUserId().equals(userId)) throw new IllegalStateException("No permission");
        entryRepository.deleteByCategoryIdAndUserId(id, userId);
        categoryRepository.delete(category);
        return "redirect:/passwords";
    }

    @PostMapping("/entry/add")
    public String addEntry(@RequestParam Long categoryId,
                           @RequestParam String name,
                           @RequestParam(required = false) String account,
                           @RequestParam(required = false) String password,
                           @RequestParam(required = false) String remark) {
        Long userId = SecurityUtil.getCurrentUserId();
        PasswordEntry entry = new PasswordEntry();
        entry.setCategoryId(categoryId);
        entry.setName(name);
        entry.setAccount(account != null ? account : "");
        entry.setPassword(password != null ? password : "");
        entry.setRemark(remark != null ? remark : "");
        entry.setUserId(userId);
        entryRepository.save(entry);
        return "redirect:/passwords";
    }

    @PostMapping("/entry/update/{id}")
    public String updateEntry(@PathVariable Long id,
                              @RequestParam String name,
                              @RequestParam(required = false) String account,
                              @RequestParam(required = false) String password,
                              @RequestParam(required = false) String remark) {
        Long userId = SecurityUtil.getCurrentUserId();
        PasswordEntry entry = entryRepository.findById(id).orElseThrow();
        if (!entry.getUserId().equals(userId)) throw new IllegalStateException("No permission");
        entry.setName(name);
        entry.setAccount(account != null ? account : "");
        entry.setPassword(password != null ? password : "");
        entry.setRemark(remark != null ? remark : "");
        entryRepository.save(entry);
        return "redirect:/passwords";
    }

    @PostMapping("/entry/delete/{id}")
    public String deleteEntry(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        PasswordEntry entry = entryRepository.findById(id).orElseThrow();
        if (!entry.getUserId().equals(userId)) throw new IllegalStateException("No permission");
        entryRepository.delete(entry);
        return "redirect:/passwords";
    }

    // ===== 智能体 JSON API =====

    @GetMapping("/api/search")
    @ResponseBody
    public Map<String, Object> apiSearch(@RequestParam String keyword,
                                         @RequestParam(defaultValue = "1") Long userId) {
        List<PasswordEntry> entries = entryRepository.searchByKeyword(userId, keyword.trim());
        List<PasswordCategory> allCategories = categoryRepository.findByUserIdOrderBySortOrderAsc(userId);
        Map<Long, String> categoryMap = allCategories.stream()
                .collect(Collectors.toMap(PasswordCategory::getId, PasswordCategory::getName));

        List<Map<String, Object>> results = entries.stream().map(e -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", e.getId());
            item.put("category", categoryMap.getOrDefault(e.getCategoryId(), "未分类"));
            item.put("name", e.getName());
            item.put("account", e.getAccount());
            item.put("password", e.getPassword());
            item.put("remark", e.getRemark());
            return item;
        }).collect(Collectors.toList());

        return Map.of("results", results);
    }

    @PostMapping("/api/entry/add")
    @ResponseBody
    public Map<String, Object> apiAddEntry(@RequestBody Map<String, String> body) {
        Long userId = Long.parseLong(body.getOrDefault("userId", "1"));
        String categoryName = body.getOrDefault("category", "").trim();
        String name = body.getOrDefault("name", "").trim();

        if (name.isEmpty()) {
            return Map.of("success", false, "message", "名称不能为空");
        }

        Long categoryId;
        if (categoryName.isEmpty()) {
            categoryId = categoryRepository.findByUserIdOrderBySortOrderAsc(userId)
                    .stream().findFirst().map(PasswordCategory::getId).orElse(null);
            if (categoryId == null) {
                PasswordCategory cat = new PasswordCategory();
                cat.setName("默认分类");
                cat.setUserId(userId);
                categoryRepository.save(cat);
                categoryId = cat.getId();
            }
        } else {
            categoryId = categoryRepository.findByUserIdOrderBySortOrderAsc(userId).stream()
                    .filter(c -> c.getName().equals(categoryName))
                    .findFirst().map(PasswordCategory::getId).orElseGet(() -> {
                        PasswordCategory cat = new PasswordCategory();
                        cat.setName(categoryName);
                        cat.setUserId(userId);
                        categoryRepository.save(cat);
                        return cat.getId();
                    });
        }

        PasswordEntry entry = new PasswordEntry();
        entry.setCategoryId(categoryId);
        entry.setName(name);
        entry.setAccount(body.getOrDefault("account", ""));
        entry.setPassword(body.getOrDefault("password", ""));
        entry.setRemark(body.getOrDefault("remark", ""));
        entry.setUserId(userId);
        entryRepository.save(entry);

        return Map.of("success", true, "id", entry.getId());
    }
}
