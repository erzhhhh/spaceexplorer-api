# SpaceExplorer API

REST API for a space news feed. The service fetches articles from the [Spaceflight News API](https://api.spaceflightnewsapi.net/v4/docs/) (SNAPI), stores them in PostgreSQL and serves them to clients with pagination.

![Tests](https://github.com/erzhhhh/spaceexplorer-api/actions/workflows/tests.yml/badge.svg)

## Live demo

The API runs at `http://64.226.126.101:8080`.

- Interactive docs: http://64.226.126.101:8080/swagger-ui.html
- Example request: http://64.226.126.101:8080/api/v2/articles?size=5

Articles are imported from SNAPI every 15 minutes, so the feed stays current.

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
| Docker Compose | local and production runtime |
| GitHub Actions | test run on every pull request |

## Running locally

Requires Docker.

```bash
docker compose up --build
```

This starts PostgreSQL and the application. The API is available at
`http://localhost:8080`, interactive docs at `http://localhost:8080/swagger-ui.html`.

To work on the code, start only the database and run the application from your IDE:

```bash
docker compose up -d db
./mvnw spring-boot:run
```

## API

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/articles` | list articles, offset pagination |
| `GET` | `/api/v2/articles` | list articles, cursor pagination |
| `POST` | `/api/admin/import` | trigger an import from SNAPI manually (requires the `X-Api-Key` header) |
| `GET` | `/actuator/health` | application and database status |

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

### POST /api/admin/import

Triggers an import without waiting for the schedule. The endpoint writes to the database and calls an external API, so it is closed with an API key:

```bash
curl -X POST http://localhost:8080/api/admin/import \
  -H "X-Api-Key: local-dev-key"
```

The public article endpoints need no key.

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
| `401` | missing or wrong `X-Api-Key` on an admin endpoint |
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

## Deployment

The service runs on a DigitalOcean droplet (Ubuntu) with Docker Compose. Updating it:

```bash
git pull
docker compose up -d --build
```

`compose.prod.yaml` is layered on top of the base file and changes two things:

- the database port mapping is removed, so PostgreSQL is reachable only by the application inside the Docker network and not from the internet
- passwords and the admin API key come from an `.env` file that lives on the server and is never committed

The droplet firewall allows only SSH and port 8080. Note that Docker publishes ports around the firewall, which is why the mapping has to be dropped in the overlay rather than blocked with firewall rules.

## Configuration

| Property | Default | Purpose |
|---|---|---|
| `snapi.base-url` | `https://api.spaceflightnewsapi.net/v4` | external API address |
| `app.scheduling.enabled` | `true` | scheduled import; turned off in tests |
| `app.admin.api-key` | `local-dev-key` | key for admin endpoints, from `ADMIN_API_KEY` |
| `spring.http.client.connect-timeout` | `3s` | connection timeout for SNAPI |
| `spring.http.client.read-timeout` | `5s` | read timeout for SNAPI |
