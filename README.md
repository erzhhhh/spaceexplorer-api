# SpaceExplorer API

REST API for a space news feed. The service fetches articles from the [Spaceflight News API](https://api.spaceflightnewsapi.net/v4/docs/) (SNAPI), stores them in PostgreSQL and serves them to clients with pagination.

![Tests](https://github.com/erzhhhh/spaceexplorer-api/actions/workflows/test.yaml/badge.svg)

## Features

- Two ways to read the feed: offset pagination and cursor pagination
- Imports fresh articles from SNAPI every 15 minutes, saving new and updated ones
- Stores articles in PostgreSQL with the schema managed by Flyway migrations
- Returns errors as `ProblemDetail` (RFC 9457)
- Keeps serving articles from the database when SNAPI is unavailable

## Stack

| Technology | Purpose |
|---|---|
| Java 21, Spring Boot 4 | application foundation |
| Spring Data JPA, Hibernate | database access |
| PostgreSQL 16 | article storage |
| Flyway | schema migrations |
| Maven | build |
| JUnit 5, Mockito, AssertJ | tests |
| Testcontainers | tests against a real database |
| GitHub Actions | test run on every pull request |

## Running locally

Requires JDK 21 and Docker.

```bash
# start PostgreSQL
docker compose up -d

# run the application
./mvnw spring-boot:run
```

The application starts on `http://localhost:8080`. It imports articles from SNAPI on startup and every 15 minutes after that, so the database fills up on its own.

Check that it works:

```bash
curl "http://localhost:8080/api/v2/articles?size=5"
```

## API

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/articles` | list articles, offset pagination |
| `GET` | `/api/v2/articles` | list articles, cursor pagination |
| `POST` | `/api/admin/import` | trigger an import from SNAPI manually |

### GET /api/v2/articles

Parameters: `cursor` (optional, points to the next page), `size` (1–100, defaults to 20).

```json
{
  "content": [
    {
      "id": 31842,
      "title": "Starship completes eleventh flight test",
      "url": "https://example.com/starship-flight-11",
      "imageUrl": "https://example.com/starship.jpg",
      "newsSite": "NASASpaceflight",
      "summary": "The vehicle splashed down in the Indian Ocean...",
      "publishedAt": "2026-09-20T14:30:00Z"
    }
  ],
  "nextCursor": "MjAyNi0wOS0yMFQxNDozMDowMFo6MzE4NDI="
}
```

Pass the `nextCursor` value as the `cursor` parameter to get the next page. When there are no more pages, `nextCursor` is `null`.

### GET /api/articles

Parameters: `page` (defaults to 0), `size` (defaults to 20, max 100), `sort`. Articles are sorted by publication date, newest first.

### Errors

```json
{
  "type": "about:blank",
  "title": "Invalid cursor",
  "status": 400,
  "detail": "The provided cursor is malformed.",
  "instance": "/api/v2/articles"
}
```

| Status | When |
|---|---|
| `400` | malformed cursor, or `size` outside 1–100 |
| `503` | SNAPI is unreachable during an import |

## Why two pagination styles

Offset pagination (`page` and `size`) is convenient for jumping to an arbitrary page, but it fits a news feed poorly: while the user scrolls, new articles are added at the top of the list, so records at page boundaries get duplicated or skipped.

Cursor pagination avoids that. The cursor points at a specific article (`publishedAt` + `id`), so the next page always starts exactly where the previous one ended. That is why the mobile client uses `/api/v2/articles`.

The query relies on a composite index on `(published_at, id)` and on row-value comparison, so the database does not walk through skipped rows the way it does with `OFFSET`.

## Tests

```bash
./mvnw verify
```

Integration tests need Docker running: they start PostgreSQL through Testcontainers.

| Layer | Tooling | What is covered |
|---|---|---|
| Service | JUnit + Mockito | import logic and cursor assembly |
| Repository | Testcontainers | SQL queries, ordering, cursor boundaries |
| Controllers | `@WebMvcTest` | routes, parameters, JSON, error codes |
| SNAPI client | `@RestClientTest` | outgoing request, response parsing, failures |
| Import | `@SpringBootTest` | the transaction rolls back entirely on failure |

Tests run automatically on every pull request to `main`.

## Configuration

| Property | Default | Purpose |
|---|---|---|
| `snapi.base-url` | `https://api.spaceflightnewsapi.net/v4` | external API address |
| `app.scheduling.enabled` | `true` | scheduled import; turned off in tests |
| `spring.http.client.connect-timeout` | `3s` | connection timeout for SNAPI |
| `spring.http.client.read-timeout` | `5s` | read timeout for SNAPI |