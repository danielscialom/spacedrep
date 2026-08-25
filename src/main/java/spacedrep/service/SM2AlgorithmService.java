package spacedrep.service;

import spacedrep.model.Card;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

@Service
public class SM2AlgorithmService {

    public void calculateSM2(Card card, int grade){
        if (grade < 0 || grade > 5){
            throw new IllegalArgumentException("Grade must be between 0 and 5");
        }

        double currEf = card.getEaseFactor();
        int repetitions = card.getRepetitionNumber();
        int interval;

        if (grade < 3){
            repetitions = 0;
            interval = 1;
        } else {
            if (repetitions == 0){
                interval = 1;
            } else if (repetitions == 1) {
                interval = 6;
            }
            else {
                interval = (int) Math.round(card.getIntervalDays() * currEf);
            }
            repetitions++;
        }
        double newEf = calculateEF(currEf, grade);
        updateCard(card, repetitions, newEf, interval);
    }

    private double calculateEF(double currEf, int q){
        double newEf = currEf + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02));
        newEf = Math.max(1.3, newEf); // optimal minimum to avoid too much repetitions
        return newEf;
    }

    private void updateCard(Card card, int repetitions, double ef, int interval){
        card.setRepetitionNumber(repetitions);
        card.setEaseFactor(ef);
        card.setIntervalDays(interval);
        card.setNextReviewDate(LocalDate.now().plusDays(interval));
    }
}
