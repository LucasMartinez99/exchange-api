# Currency Converter API

A production-quality currency conversion REST API built with Java and Spring Boot.  
Consumes a live exchange rate API, persists conversion history to PostgreSQL, and exposes analytics endpoints.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.4.4 |
| External API | Spring Cloud OpenFeign |
| Database | PostgreSQL 16 + Flyway migrations |
| Security | Spring Security (HTTP Basic Auth) |
| API Docs | OpenAPI 3 / Swagger UI |
| Build | Maven |
| Container | Docker + Docker Compose |
| Testing | JUnit 5 + Mockito |

## Architecture

The project follows a classic **layered architecture** — each layer has a single responsibility and depends only on the layer below it.

```
src/main/java/com/lucas/msla/
│
├── controller/         # REST endpoints — receives requests, delegates to the service
├── service/            # Business logic — orchestrates Feign client + repository
├── repository/         # Data access — Spring Data JPA queries
├── entity/             # JPA entities mapped to PostgreSQL tables
├── dto/                # Request and response objects (what the API receives and returns)
├── mapper/             # Converts between entities and DTOs
├── feign/
│   ├── ExchangeRateClient.java        # Feign interface — calls the external exchange rate API
│   └── dto/ExchangeRateApiResponse.java  # Typed DTO for the external API response
├── exception/          # Custom exceptions + GlobalExceptionHandler
└── config/             # Spring Security configuration
```

### Request flow

```
HTTP Request
    → ExchangeRateController
        → ExchangeRateService  (@Transactional)
            → ExchangeRateClient (Feign → external API)
            → ConversionHistoryRepository (save to PostgreSQL)
        → ConversionMapper
    → HTTP Response
```

## Features

- Convert between currencies using a live external exchange rate API
- Persist every conversion to PostgreSQL with a full timestamp
- Query conversion history by date range
- Get the total amount converted per target currency
- HTTP Basic Auth protecting all business endpoints
- Swagger UI for interactive API exploration
- Flyway-managed schema — versioned SQL migrations, not auto-generated DDL
- Docker Compose with database health check — app only starts when PostgreSQL is ready

## API Endpoints

All endpoints except Swagger require **HTTP Basic Auth**.

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/exchange/convert` | Convert an amount between two currencies |
| GET | `/api/v1/exchange/history` | List conversion history between two dates |
| GET | `/api/v1/exchange/summary` | Total converted amount for a given currency |

Full interactive documentation: `http://localhost:8081/swagger-ui.html`

### POST `/api/v1/exchange/convert`

**Request**
```json
{
  "from": "USD",
  "to": "PEN",
  "amount": 10
}
```

**Response**
```json
{
  "from": "USD",
  "to": "PEN",
  "amount": 10,
  "exchangeRate": 3.426126,
  "convertedAmount": 34.26126,
  "date": "2026-03-07",
  "timestamp": "2026-03-07T14:32:10.123",
  "success": true
}
```

### GET `/api/v1/exchange/history?startDate=2026-03-01&endDate=2026-03-31`

Returns a list of all conversions recorded in the given date range, each with a full `LocalDateTime` timestamp.

### GET `/api/v1/exchange/summary?currency=PEN`

Returns the total amount converted into the given currency across all recorded conversions.

## Running Locally

**Prerequisites:** Docker, Java 17, Maven

```bash
# 1. Clone the repo
git clone https://github.com/LucasMartinez99/msla.git
cd msla

# 2. Set up environment variables
cp .env.example .env
# Fill in .env: DB credentials, exchange rate API key, Basic Auth username/password
# Get a free API key at: https://apilayer.com/marketplace/exchangerates_data-api

# 3. Start the full stack (app + PostgreSQL)
docker compose up -d

# 4. API is live at http://localhost:8081
# 5. Swagger UI at http://localhost:8081/swagger-ui.html
```

To run only the database and use Maven for development:

```bash
docker compose up -d db
mvn spring-boot:run
```

## Running Tests

```bash
mvn test
```

Tests use Mockito — no Docker or database required. Coverage includes:

- Successful currency conversion flow
- External API failure (`success: false`) → throws `ExternalApiException`
- History query by date range
- Summary by currency

## Quick API Demo

```bash
# Convert 100 USD to PEN
curl -u admin:yourpassword -X POST http://localhost:8081/api/v1/exchange/convert \
  -H "Content-Type: application/json" \
  -d '{"from":"USD","to":"PEN","amount":100}'

# Get history for March 2026
curl -u admin:yourpassword \
  "http://localhost:8081/api/v1/exchange/history?startDate=2026-03-01&endDate=2026-03-31"

# Total converted to PEN
curl -u admin:yourpassword \
  "http://localhost:8081/api/v1/exchange/summary?currency=PEN"
```

## Key Engineering Decisions

**Typed Feign response DTO** — the external API response is deserialized into `ExchangeRateApiResponse` (a Java record with a nested `RateInfo`). No raw `Map<String, Object>` casts — if the external API changes a field name, it fails cleanly at deserialization, not buried in a `ClassCastException` at runtime.

**Flyway owns the schema** — Hibernate is set to `ddl-auto: validate`. Schema changes are versioned SQL files (`V1__init.sql`), not auto-generated DDL. This is the only safe approach for a production database.

**`LocalDateTime` for conversion history** — storing just a `LocalDate` would make multiple conversions on the same day indistinguishable. Every record has a full timestamp so history can be sorted and queried precisely.

**`ExternalApiException` → 502 Bad Gateway** — external API failures are a distinct error category from internal errors. The `GlobalExceptionHandler` maps them to HTTP 502 so the caller knows the problem is upstream, not in this service.

**`@Transactional` on `convertCurrency`** — the external API call and the database save are wrapped in a transaction. If the save fails, the caller gets a clean error and no partial state is written.

**All secrets in environment variables** — API key, database credentials, and Basic Auth password are never hardcoded. The `.env.example` documents every required variable.
