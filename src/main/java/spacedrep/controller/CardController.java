package spacedrep.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spacedrep.dto.CreateCardRequest;
import spacedrep.dto.ReviewCardRequest;
import spacedrep.model.Card;
import spacedrep.service.CardService;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    public ResponseEntity<List<Card>> getAllCards() {
        List<Card> cards = cardService.getAllCards();
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/due")
    public ResponseEntity<List<Card>> getCardsDueToday() {
        List<Card> dueCards = cardService.getCardsDueToday();
        return ResponseEntity.ok(dueCards);
    }

    @PostMapping
    public ResponseEntity<Card> createCard(@Valid @RequestBody CreateCardRequest request) {
        Card created = cardService.createCard(request.getFront(), request.getBack());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/{id}/review")
    public ResponseEntity<Card> reviewCard(
            @PathVariable Long id,
            @Valid @RequestBody ReviewCardRequest request) {
        Card updated = cardService.reviewCard(id, request.getGrade());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllCards(
            @RequestParam(name = "confirm", defaultValue = "false") boolean confirm) {
        if (!confirm) {
            return ResponseEntity.badRequest().build();
        }
        cardService.deleteAllCards();
        return ResponseEntity.noContent().build();
    }
}
