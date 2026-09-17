# Spaced Repetition Flashcards

A full-stack flashcard application implementing the SuperMemo-2 (SM-2) spaced repetition algorithm. The system schedules reviews based on memory retention intervals, separating scheduled daily reviews from unrated practice mode.

Built with Java, Spring Boot, PostgreSQL, and a vanilla JavaScript frontend.

---

## Architecture & Data Flow

```text
Browser (Vanilla JS / Fetch API)
       │
       ▼  HTTP / JSON
Spring Boot REST Controllers (`CardController`)
       │
       ▼  Business Logic & SM-2 Math
Service Layer (`CardService`)
       │
       ▼  ORM / Queries
Spring Data JPA Repository
       │
       ▼  Port 5432
PostgreSQL (Dockerized)
```

---


## SM-2 Implementation Summary

The algorithm calculates the next review interval using the user's recall quality (q = 1 to 5):

1. **Grades:**
    * `1–2`: Failure / Blackout — resets `repetitionNumber` to 0; card is rescheduled for tomorrow (Interval = 1).
    * `3`: Correct with significant effort — keeps the streak but lowers the `easeFactor`.
    * `4`: Correct with slight hesitation — normal interval progression.
    * `5`: Instant recall — increases the interval progression rate.
2. **Ease Factor (EF) Adjustment:**
   `EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))`
   *Floor threshold applied at EF >= 1.3 to prevent cards from getting stuck in permanent high-frequency loops.*
3. **Interval Progression (I):**
    * `I(1) = 1 day`
    * `I(2) = 6 days`
    * `I(n) = I(n-1) * EF` (for n > 2)

---

## REST API Specification

| Method | Endpoint | Description | Status Codes |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/cards` | Get all flashcards | `200 OK` |
| `GET` | `/api/cards/due` | Get cards due for review on or before today | `200 OK` |
| `POST` | `/api/cards` | Create a new card (`front`, `back`) | `201 Created`, `400 Bad Request` |
| `POST` | `/api/cards/{id}/review` | Submit SM-2 grade (`grade: 1-5`) | `200 OK`, `404 Not Found` |
| `DELETE` | `/api/cards/{id}` | Delete a card by ID | `204 No Content`, `404 Not Found` |
| `DELETE` | `/api/cards?confirm=true` | Delete all cards (Requires confirmation parameter) | `204 No Content`, `400 Bad Request` |

---

## Tech Stack

* **Backend:** Java 17, Spring Boot 3 (Spring Web, Spring Data JPA, Jakarta Validation)
* **Database:** PostgreSQL 16
* **Frontend:** Vanilla JavaScript (Fetch API, DOM manipulation), CSS3, HTML5
* **Infrastructure:** Docker, Docker Compose

---

## Setup & Running Locally

### 1. Prerequisites
* Java 17+ installed
* Docker Desktop installed and running

### 2. Run Database
Start the PostgreSQL container:
```bash
docker compose up -d
```

### 3. Run Backend Application
Using Maven:
```bash
./mvnw spring-boot:run
```
*(On Windows PowerShell: `.\mvnw.cmd spring-boot:run`)*

### 4. Access the App
Open your browser and navigate to:
```text
http://localhost:8080
```