package spacedrep.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spacedrep.model.Card;
import spacedrep.repository.CardRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private SM2AlgorithmService sm2Service;

    @InjectMocks
    private CardService cardService;

    @Test
    void testCreateCard_Success() {
        Card savedCard = new Card("Question", "Answer");
        when(cardRepository.save(any(Card.class))).thenReturn(savedCard);
        Card result = cardService.createCard("Question", "Answer");

        assertNotNull(result);
        assertEquals("Question", result.getFront());
        assertEquals("Answer", result.getBack());

        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void testCreateCard_WithEmptyFront_ThrowsExceptionAndNeverSaves() {
        try {
            cardService.createCard("   ", "Answer");
            fail("Expected IllegalArgumentException for empty front, but none was thrown");
        } catch (IllegalArgumentException e) {
            // Exception thrown - successful test
        }

        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void testGetCardsDueToday() {
        List<Card> mockDueCards = new ArrayList<>();
        mockDueCards.add(new Card("Q1", "A1"));
        mockDueCards.add(new Card("Q2", "A2"));

        when(cardRepository.findByNextReviewDateLessThanEqual(any(LocalDate.class)))
                .thenReturn(mockDueCards);

        List<Card> result = cardService.getCardsDueToday();

        assertEquals(2, result.size());
        assertEquals("Q1", result.get(0).getFront());
        verify(cardRepository, times(1)).findByNextReviewDateLessThanEqual(any(LocalDate.class));
    }

    @Test
    void testReviewCard_Success() {
        Card existingCard = new Card("Question", "Answer");

        when(cardRepository.findById(1L)).thenReturn(Optional.of(existingCard));
        when(cardRepository.save(existingCard)).thenReturn(existingCard);

        Card result = cardService.reviewCard(1L, 4);

        assertNotNull(result);
        verify(sm2Service, times(1)).calculateSM2(existingCard, 4);
        verify(cardRepository, times(1)).save(existingCard);
    }

    @Test
    void testReviewCard_WhenCardNotFound_ThrowsException() {
        when(cardRepository.findById(999L)).thenReturn(Optional.empty());

        try {
            cardService.reviewCard(999L, 4);
            fail("Expected IllegalArgumentException when card is not found, but none was thrown");
        } catch (IllegalArgumentException e) {
            // Exception thrown - successful test
        }

        verify(sm2Service, never()).calculateSM2(any(Card.class), anyInt());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void testGetAllCards() {
        List<Card> allCards = new ArrayList<>();
        allCards.add(new Card("Front 1", "Back 1"));
        allCards.add(new Card("Front 2", "Back 2"));

        when(cardRepository.findAll()).thenReturn(allCards);

        List<Card> result = cardService.getAllCards();

        assertEquals(2, result.size());
        assertEquals("Front 1", result.get(0).getFront());
        verify(cardRepository, times(1)).findAll();
    }

    @Test
    void testDeleteCard_Success() {
        when(cardRepository.existsById(1L)).thenReturn(true);

        cardService.deleteCard(1L);

        verify(cardRepository, times(1)).existsById(1L);
        verify(cardRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteCard_NotFound_ThrowsExceptionAndNeverDeletes() {
        when(cardRepository.existsById(999L)).thenReturn(false);

        try {
            cardService.deleteCard(999L);
            fail("Expected IllegalArgumentException for non-existing card, but none was thrown");
        } catch (IllegalArgumentException e) {
            // Exception thrown - successful test
        }

        verify(cardRepository, never()).deleteById(anyLong());
    }
}
