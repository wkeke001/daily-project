package com.lingke.todo.service;

import com.lingke.todo.entity.Card;
import com.lingke.todo.entity.CardItem;
import com.lingke.todo.repository.CardItemRepository;
import com.lingke.todo.repository.CardRepository;
import com.lingke.todo.security.SecurityUtil;
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
        Long userId = SecurityUtil.getCurrentUserId();
        return cardRepository.findAllWithItemsByUserId(userId);
    }

    public Card addCard(String name) {
        Long userId = SecurityUtil.getCurrentUserId();
        Card card = new Card();
        card.setName(name);
        card.setUserId(userId);
        return cardRepository.save(card);
    }

    public void updateCard(Long id, String name) {
        Long userId = SecurityUtil.getCurrentUserId();
        cardRepository.findByIdAndUserId(id, userId).ifPresent(card -> {
            card.setName(name);
            cardRepository.save(card);
        });
    }

    public void deleteCard(Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        cardRepository.findByIdAndUserId(id, userId).ifPresent(card ->
            cardRepository.delete(card)
        );
    }

    public void addItem(Long cardId, String url, String account, String password, String remark) {
        Long userId = SecurityUtil.getCurrentUserId();
        cardRepository.findByIdAndUserId(cardId, userId).ifPresent(card -> {
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
        Long userId = SecurityUtil.getCurrentUserId();
        cardItemRepository.findById(itemId).ifPresent(item -> {
            if (item.getCard() != null && userId.equals(item.getCard().getUserId())) {
                item.setUrl(url);
                item.setAccount(account);
                item.setPassword(password);
                item.setRemark(remark);
                cardItemRepository.save(item);
            }
        });
    }

    public void deleteItem(Long itemId) {
        Long userId = SecurityUtil.getCurrentUserId();
        cardItemRepository.findById(itemId).ifPresent(item -> {
            if (item.getCard() != null && userId.equals(item.getCard().getUserId())) {
                cardItemRepository.delete(item);
            }
        });
    }
}
