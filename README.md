# Spaced Repetition Flashcards

A full-stack flashcard system implementing the SuperMemo-2 (SM-2) algorithm to optimize long-term memory retention. The application separates daily scheduled reviews from unrated free practice sessions to preserve retention curve integrity.

Built with Java 17, Spring Boot 3, PostgreSQL, Docker, and a Vanilla JavaScript single-page interface.

---

## Architecture & Design Patterns

The backend code is divided into clear layers with Separation of Concerns (SoC):

```text
Browser (Plain JavaScript)
       │
       ▼  HTTP Requests
Controllers (`CardController`)
       │
       ▼
Main Service (`CardService`) ────► Core Algorithm (`SM2AlgorithmService`)
       │
       ▼
Database Layer (`CardRepository`)
       │
       ▼  Port 5432
PostgreSQL (Docker)
```

### Key Engineering Decisions
* **Separated Math Logic:** The SM-2 math calculations are kept in their own service (`SM2AlgorithmService`). This makes the math independent of the database and easy to test.
* **Separation of Review vs. Practice:** Daily reviews updates the memory score. Free practice shuffles the cards without updating scores, preventing short-term practice from breaking the long-term schedule.
* **Missed Days Handling:** The database query uses `nextReviewDate <= CURRENT_DATE` (less than or equal to), ensuring cards from missed sessions remain queued rather than being dropped.

---

## The SM-2 Algorithm

Recall quality is rated on a scale of $q \in [1, 5]$:

1. **Grades:**
   * `1–2 (Failure)`: Resets `repetitionNumber` to 0. Card rescheduled for the next day ($I = 1$).
   * `3 (Hard)`: Correct recall with difficulty. Streak preserved, Ease Factor decreases.
   * `4 (Good)`: Correct recall with hesitation. Normal interval progression.
   * `5 (Easy)`: Perfect recall with zero hesitation. Interval progression increases.
2. **Ease Factor (EF) Formula:**
   `EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))`
   *Bounded by a hard minimum of EF >= 1.3 to avoid permanent stagnation loops.*
3. **Interval Progression (I):**
   * `I(1) = 1 day`
   * `I(2) = 6 days`
   * `I(n) = I(n-1) * EF` (for n > 2)

---

## Automated Tests

The project includes different levels of testing (configured in `src/test/resources`):

* **Algorithm Tests (`SM2AlgorithmServiceTest`):** Fast unit tests that check the math formula, grade changes, and minimum limits without needing a database.
* **Service Tests (`CardServiceTest`):** Uses Mockito to test how the service saves data and interacts with the algorithm.
* **Integration Tests (`CardControllerIntegrationTest`):** Uses `MockMvc` to test real HTTP requests, check error handling (`400 Bad Request`), and verify the API responses.
---

## REST API Specification

| Method | Endpoint | Description | Status Codes |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/cards` | Fetch all cards | `200 OK` |
| `GET` | `/api/cards/due` | Fetch cards scheduled on or before today | `200 OK` |
| `POST` | `/api/cards` | Create a flashcard (`front`, `back`) | `201 Created`, `400 Bad Request` |
| `POST` | `/api/cards/{id}/review` | Submit recall grade (`grade: 1-5`) | `200 OK`, `404 Not Found` |
| `DELETE` | `/api/cards/{id}` | Delete card by ID | `204 No Content`, `404 Not Found` |
| `DELETE` | `/api/cards?confirm=true` | Bulk delete all cards (Requires confirmation) | `204 No Content`, `400 Bad Request` |

---

## Tech Stack

* **Backend:** Java 17, Spring Boot 3, Spring Data JPA, Jakarta Validation
* **Database:** PostgreSQL 16
* **Testing:** JUnit 5, Mockito, MockMvc
* **Frontend:** Plain HTML, CSS, and JavaScript (Fetch API)
* **Tools:** Docker, Docker Compose, Maven Wrapper

---

## Setup & Running Locally

### 1. Start the Database
Start the PostgreSQL database using Docker:
```bash
docker compose up -d
```

### 2. Run the Tests
Run all unit and integration tests to make sure everything works:
```bash
./mvnw test
```
*(On Windows: `.\mvnw.cmd test`)*

### 3. Start the Server
```bash
./mvnw spring-boot:run
```
*(On Windows: `.\mvnw.cmd spring-boot:run`)*

### 4. Open the App
Go to your browser and open:
```text
http://localhost:8080
```