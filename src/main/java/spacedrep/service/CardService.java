package spacedrep.service;

import org.springframework.stereotype.Service;
import spacedrep.model.Card;
import spacedrep.repository.CardRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final SM2AlgorithmService sm2Service;

    public CardService(CardRepository cardRepository, SM2AlgorithmService sm2Service) {
        this.cardRepository = cardRepository;
        this.sm2Service = sm2Service;
    }

    public List<Card> getCardsDueToday() {
        return cardRepository.findByNextReviewDateLessThanEqual(LocalDate.now());
    }

    public Card createCard(String front, String back){
        if (front == null || front.trim().isEmpty()) {
            throw new IllegalArgumentException("Card front cannot be null or empty");
        }

        if (back == null || back.trim().isEmpty()) {
            throw new IllegalArgumentException("Card back cannot be null or empty");
        }

        Card newCard = new Card(front.trim(), back.trim());
        return cardRepository.save(newCard);
    }

    public Optional<Card> getCardById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Card ID cannot be null");
        }
        return cardRepository.findById(id);  //Returns an "Optional" object (empty object) when id is not found.
    }

    public Card reviewCard(Long id, int grade) {
        if (id == null) {
            throw new IllegalArgumentException("Card ID cannot be null");
        }

        Optional<Card> optionalCard = cardRepository.findById(id);
        if (optionalCard.isEmpty()) {   // In case Optional object is empty.
            throw new IllegalArgumentException("Card not found with id: " + id);
        }

        Card card = optionalCard.get();
        sm2Service.calculateSM2(card, grade);
        return cardRepository.save(card);
    }

    public List<Card> getAllCards() {
        return cardRepository.findAll();
    }

    public void deleteCard(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Card ID cannot be null");
        }

        if (!cardRepository.existsById(id)) {
            throw new IllegalArgumentException("Card not found with id: " + id);
        }

        cardRepository.deleteById(id);
    }
}
