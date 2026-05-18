package com.lingke.todo.service;

import com.lingke.todo.entity.Card;
import com.lingke.todo.entity.CardItem;
import com.lingke.todo.repository.CardItemRepository;
import com.lingke.todo.repository.CardRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final CardItemRepository cardItemRepository;

    public CardService(CardRepository cardRepository, CardItemRepository cardItemRepository) {
        this.cardRepository = cardRepository;
        this.cardItemRepository = cardItemRepository;
    }

    public List<Card> findAllCards() {
        return cardRepository.findAllWithItems();
    }

    public Card addCard(String name) {
        Card card = new Card();
        card.setName(name);
        return cardRepository.save(card);
    }

    public void updateCard(Long id, String name) {
        cardRepository.findById(id).ifPresent(card -> {
            card.setName(name);
            cardRepository.save(card);
        });
    }

    public void deleteCard(Long id) {
        cardRepository.deleteById(id);
    }

    public void addItem(Long cardId, String url, String account, String password, String remark) {
        cardRepository.findById(cardId).ifPresent(card -> {
            CardItem item = new CardItem();
            item.setUrl(url);
            item.setAccount(account);
            item.setPassword(password);
            item.setRemark(remark);
            item.setCard(card);
            cardItemRepository.save(item);
        });
    }

    public void updateItem(Long itemId, String url, String account, String password, String remark) {
        cardItemRepository.findById(itemId).ifPresent(item -> {
            item.setUrl(url);
            item.setAccount(account);
            item.setPassword(password);
            item.setRemark(remark);
            cardItemRepository.save(item);
        });
    }

    public void deleteItem(Long itemId) {
        cardItemRepository.deleteById(itemId);
    }
}
