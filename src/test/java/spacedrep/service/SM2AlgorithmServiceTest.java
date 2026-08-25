package spacedrep.service;

import org.junit.jupiter.api.Test;
import spacedrep.model.Card;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class SM2AlgorithmServiceTest {

    private final SM2AlgorithmService service = new SM2AlgorithmService();

    @Test
    void testFirstSuccessfulReview() {
        Card card = new Card("Question", "Answer");

        service.calculateSM2(card, 4);

        assertEquals(1, card.getRepetitionNumber(), "Repetition count must increase to 1");
        assertEquals(1, card.getIntervalDays(), "First repetition interval must be 1 day");
        assertEquals(2.5, card.getEaseFactor(), "Grade 4 should keep Ease Factor at 2.5");
        assertEquals(LocalDate.now().plusDays(1), card.getNextReviewDate(), "Next review date must be tomorrow");
    }

    @Test
    void testConsecutiveSuccessfulReviewsProgression() {
        Card card = new Card("Question", "Answer");

        // 1: 1 day interval
        service.calculateSM2(card, 4);
        assertEquals(1, card.getIntervalDays(), "First review interval should be 1");
        assertEquals(1, card.getRepetitionNumber(), "Repetition counter should be 1");

        // 2: 6 days interval
        service.calculateSM2(card, 4);
        assertEquals(6, card.getIntervalDays(), "Second review interval should be 6");
        assertEquals(2, card.getRepetitionNumber(), "Repetition counter should be 2");

        // 3: Previous interval * EF (6 * 2.5 = 15 days)
        service.calculateSM2(card, 4);
        assertEquals(15, card.getIntervalDays(), "Third review interval should be 15");
        assertEquals(3, card.getRepetitionNumber(), "Repetition counter should be 3");
    }

    @Test
    void testFailureResetsRepetitionsAndInterval() {
        Card card = new Card("Question", "Answer");

        // Perform two successful reviews first
        service.calculateSM2(card, 5);
        service.calculateSM2(card, 5);
        assertEquals(2, card.getRepetitionNumber());

        // Review with a failing grade (1)
        service.calculateSM2(card, 1);

        assertEquals(0, card.getRepetitionNumber(), "Failure must reset repetitions to 0");
        assertEquals(1, card.getIntervalDays(), "Failure must reset interval to 1 day");
    }

    @Test
    void testEaseFactorFloorLimit() {
        Card card = new Card("Question", "Answer");

        // Simulate multiple consecutive failures
        for (int i = 0; i < 10; i++) {
            service.calculateSM2(card, 0);
        }

        assertEquals(1.3, card.getEaseFactor(), 0.001, "Ease Factor should never drop below 1.3");
    }

    @Test
    void testInvalidGradesThrowException() {  // Test exception in illegal cases
        Card card = new Card("Question", "Answer");

        // 1: Grade lower than 0
        try {
            service.calculateSM2(card, -1);
            fail("Grade below 0 should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Exception thrown - successful test
        }

        // 2: Grade higher than 5
        try {
            service.calculateSM2(card, 6);
            fail("Grade above 5 should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Exception thrown - successful test
        }
    }
}
