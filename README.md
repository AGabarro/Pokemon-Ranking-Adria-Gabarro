# Pokémon Ranking API

A Spring Boot REST API that queries PokéAPI and returns the top 5 Pokémon
ranked by weight, height, and base experience.

## How to run

**Requirements:** Java 21, Maven 3.x

```bash
mvn spring-boot:run
```

The server starts on `http://localhost:8080`. Once you see
`Tomcat started on port 8080` in the logs, the API is ready.

```bash
curl http://localhost:8080/pokemon/heaviest
```

**The first request takes around 60–90 seconds** — the app fetches and
processes ~1300 Pokémon from PokéAPI in parallel. Every subsequent request
returns in under 50ms from the in-memory cache. The cache expires after 1 hour.

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/pokemon/heaviest` | Top 5 heaviest Pokémon (kg) |
| GET | `/pokemon/tallest` | Top 5 tallest Pokémon (m) |
| GET | `/pokemon/most-experienced` | Top 5 by base experience (pts) |

## Design decisions

**Fetching strategy:** a single call with `limit=10000` retrieves all Pokémon
names upfront rather than paginating or hardcoding a count. This means future
additions to PokéAPI are handled automatically without any code change.

**Concurrency:** each of the ~1300 individual Pokémon is fetched concurrently
using virtual threads (Java 21). `parallelStream()` was considered but ruled
out since it runs on the ForkJoinPool, which conflicts with Reactor Netty's thread
model and causes flaky behaviour under test. Virtual threads sidestep that
entirely.

**WebClient:** WebClient is Spring's current HTTP client and
the right default for new projects. It's used here in blocking mode via
`.block()`

**Caching:** `@Cacheable` (Caffeine, 1-hour TTL) sits on `PokeApiClient.fetchAll()`
rather than on a service method to cache all Pokemon details.

**Records for DTOs:** Java records are used instead of regular classes, they are immutable and the compiler generates constructors and getters automatically, removing           
boilerplate.

**Nullable baseExperience:** Some Pokémon in PokéAPI return null for base experience. It is typed as Integer (nullable) and filtered out before ranking to avoid a                
NullPointerException.

**Response buffer size:** PokeAPI responses for individual Pokémon are large (moves, sprites, abilities). The default WebClient buffer of 256KB was too small, causing many Pokémon to be silently dropped. Observed in the server logs as `DataBufferLimitException`. Raised to 5MB via `spring.codec.max-in-memory-size=5MB` in `application.properties`.

**Retry on transient failures:** running the server revealed 5–10 Pokémon failing per request due to connection resets or timeouts from PokéAPI under concurrent load. `fetchOne` now retries up to 3 times with a 300ms fixed delay. 404s are excluded from retries since a missing Pokémon will not appear on a second attempt.

**Error handling:** all failures reaching PokéAPI throw a `PokeApiException`,
caught centrally by `PokeApiExceptionHandler` (`@RestControllerAdvice`) and
returned as `503 Service Unavailable`. Internal stack traces never reach the
client.

## Testing

```bash
mvn verify
```

Runs all tests and enforces **90% line coverage** via JaCoCo.

- `PokemonServiceTest` — unit tests for ranking logic using a mocked client
- `PokeApiClientTest` — HTTP-level tests using MockWebServer (real HTTP calls,
  no mocks)
- `PokemonApiIntegrationTest` — full Spring context via `@SpringBootTest`,
  verifying all three endpoints end to end