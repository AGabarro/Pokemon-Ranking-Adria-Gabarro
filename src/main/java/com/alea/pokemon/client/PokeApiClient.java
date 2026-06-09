package com.alea.pokemon.client;

import com.alea.pokemon.dto.PokeApiPokemonResponse;
import com.alea.pokemon.exception.PokeApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

@Component
public class PokeApiClient {

    private static final Logger log = LoggerFactory.getLogger(PokeApiClient.class);

    private final WebClient webClient;

    public PokeApiClient(WebClient pokeApiWebClient) {
        this.webClient = pokeApiWebClient;
    }

    @Cacheable("allPokemon")
    public List<PokeApiPokemonResponse> fetchAll() {
        PokemonListPage page;
        try {
            page = webClient.get()
                    .uri("/pokemon?limit=10000")
                    .retrieve()
                    .bodyToMono(PokemonListPage.class)
                    .block();
        } catch (Exception e) {
            throw new PokeApiException("Failed to fetch Pokémon list", e);
        }

        if (page == null) {
            throw new PokeApiException("Pokémon list response was empty", null);
        }

        log.info("Fetching details for {} Pokémon", page.results().size());

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<PokeApiPokemonResponse>> futures = page.results().stream()
                    .map(entry -> CompletableFuture.supplyAsync(() -> fetchOne(entry.name()), executor))
                    .toList(); // all virtual threads start here
            return futures.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .toList();
        }
    }

    PokeApiPokemonResponse fetchOne(String name) {
        try {
            return webClient.get()
                    .uri("/pokemon/{name}", name)
                    .retrieve()
                    .bodyToMono(PokeApiPokemonResponse.class)
                    .block();
        } catch (Exception e) {
            log.warn("Failed to fetch pokemon={}: {}", name, e.getMessage());
            return null;
        }
    }

    private record NamedResource(String name) {}

    private record PokemonListPage(List<NamedResource> results) {}
}
