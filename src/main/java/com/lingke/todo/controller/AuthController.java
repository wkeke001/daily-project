package com.lingke.todo.controller;

import com.lingke.todo.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(@RequestParam String username,
                             @RequestParam String password,
                             Model model) {
        if (username.isBlank() || password.isBlank()) {
            model.addAttribute("error", "用户名和密码不能为空");
            return "register";
        }
        if (password.length() < 4) {
            model.addAttribute("error", "密码长度至少4位");
            return "register";
        }
        boolean success = userService.register(username.trim(), password);
        if (!success) {
            model.addAttribute("error", "用户名已存在");
            return "register";
        }
        return "redirect:/login?registered";
    }
}
