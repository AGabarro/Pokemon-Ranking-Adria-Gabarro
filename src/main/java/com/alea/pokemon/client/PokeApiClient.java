package com.alea.pokemon.client;

import com.alea.pokemon.dto.PokeApiPokemonResponse;
import com.alea.pokemon.exception.PokeApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Objects;

@Component
public class PokeApiClient {

    private static final Logger log = LoggerFactory.getLogger(PokeApiClient.class);

    private final WebClient webClient;

    public PokeApiClient(WebClient pokeApiWebClient) {
        this.webClient = pokeApiWebClient;
    }

    public List<PokeApiPokemonResponse> fetchAll() {
        PokemonListPage page = webClient.get()
                .uri("/pokemon?limit=10000")
                .retrieve()
                .bodyToMono(PokemonListPage.class)
                .block();

        if (page == null) {
            throw new PokeApiException("Pokémon list response was empty", null);
        }

        log.info("Fetching details for {} Pokémon", page.results().size());

        return page.results().parallelStream()
                .map(entry -> fetchOne(entry.name()))
                .filter(Objects::nonNull)
                .toList();
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
