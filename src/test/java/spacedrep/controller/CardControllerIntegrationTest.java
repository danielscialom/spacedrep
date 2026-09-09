package spacedrep.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import spacedrep.dto.CreateCardRequest;
import spacedrep.dto.ReviewCardRequest;
import spacedrep.model.Card;
import spacedrep.repository.CardRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private CardRepository cardRepository;

    @BeforeEach
    void setUp() {
        cardRepository.deleteAll();
    }

    @Test
    void createCard_ValidInput_Returns201AndPersistsInDatabase() throws Exception {
        CreateCardRequest request = new CreateCardRequest();
        request.setFront("Question");
        request.setBack("Answer");

        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.front").value("Question"))
                .andExpect(jsonPath("$.back").value("Answer"))
                .andExpect(jsonPath("$.repetitionNumber").value(0))
                .andExpect(jsonPath("$.easeFactor").value(2.5));

        assertThat(cardRepository.count()).isEqualTo(1);
    }

    @Test
    void createCard_BlankFront_Returns400BadRequest() throws Exception {
        CreateCardRequest request = new CreateCardRequest();
        request.setFront(""); // Blank front - invalid input
        request.setBack("Answer");

        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        assertThat(cardRepository.count()).isZero();
    }

    @Test
    void getAllCards_ReturnsCardList() throws Exception {
        Card card1 = new Card("Q1", "A1");
        Card card2 = new Card("Q2", "A2");
        cardRepository.save(card1);
        cardRepository.save(card2);

        mockMvc.perform(get("/api/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].front").value("Q1"))
                .andExpect(jsonPath("$[1].front").value("Q2"));
    }

    @Test
    void reviewCard_ValidGrade_UpdatesCardAndCalculatesInterval() throws Exception {
        Card savedCard = cardRepository.save(new Card("Apple", "תפוח"));

        ReviewCardRequest reviewRequest = new ReviewCardRequest();
        reviewRequest.setGrade(4);

        mockMvc.perform(post("/api/cards/" + savedCard.getId() + "/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repetitionNumber").value(1))
                .andExpect(jsonPath("$.intervalDays").value(1));

        Card updated = cardRepository.findById(savedCard.getId()).orElseThrow();
        assertThat(updated.getRepetitionNumber()).isEqualTo(1);
        assertThat(updated.getIntervalDays()).isEqualTo(1);
    }

    @Test
    void deleteCard_ExistingId_DeletesCardAndReturns204NoContent() throws Exception {
        Card savedCard = cardRepository.save(new Card("To Delete", "למחוק"));

        mockMvc.perform(delete("/api/cards/" + savedCard.getId()))
                .andExpect(status().isNoContent());

        assertThat(cardRepository.existsById(savedCard.getId())).isFalse();
    }
}