package com.lingke.todo.controller;

import com.lingke.todo.service.CardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("cards", cardService.findAllCards());
        return "cards";
    }

    @PostMapping("/add")
    public String addCard(@RequestParam String name) {
        cardService.addCard(name);
        return "redirect:/cards";
    }

    @PostMapping("/update/{id}")
    public String updateCard(@PathVariable Long id, @RequestParam String name) {
        cardService.updateCard(id, name);
        return "redirect:/cards";
    }

    @PostMapping("/delete/{id}")
    public String deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return "redirect:/cards";
    }

    @PostMapping("/{cardId}/items/add")
    public String addItem(@PathVariable Long cardId,
                          @RequestParam String url,
                          @RequestParam String account,
                          @RequestParam String password,
                          @RequestParam(required = false) String remark) {
        cardService.addItem(cardId, url, account, password, remark);
        return "redirect:/cards";
    }

    @PostMapping("/items/update/{itemId}")
    public String updateItem(@PathVariable Long itemId,
                             @RequestParam String url,
                             @RequestParam String account,
                             @RequestParam String password,
                             @RequestParam(required = false) String remark) {
        cardService.updateItem(itemId, url, account, password, remark);
        return "redirect:/cards";
    }

    @PostMapping("/items/delete/{itemId}")
    public String deleteItem(@PathVariable Long itemId) {
        cardService.deleteItem(itemId);
        return "redirect:/cards";
    }
}
