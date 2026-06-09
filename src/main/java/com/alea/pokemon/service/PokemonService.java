package com.alea.pokemon.service;

import com.alea.pokemon.client.PokeApiClient;
import com.alea.pokemon.dto.PokeApiPokemonResponse;
import com.alea.pokemon.dto.PokemonRankingDto;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class PokemonService {

    private final PokeApiClient pokeApiClient;

    public PokemonService(PokeApiClient pokeApiClient) {
        this.pokeApiClient = pokeApiClient;
    }

    public List<PokemonRankingDto> getHeaviest() {
        return pokeApiClient.fetchAll().stream()
                .sorted(Comparator.comparingInt(PokeApiPokemonResponse::weight).reversed())
                .limit(5)
                .map(p -> new PokemonRankingDto(p.id(), p.name(), p.weight() / 10.0, "kg"))
                .toList();
    }

    public List<PokemonRankingDto> getHighest() {
        return pokeApiClient.fetchAll().stream()
                .sorted(Comparator.comparingInt(PokeApiPokemonResponse::height).reversed())
                .limit(5)
                .map(p -> new PokemonRankingDto(p.id(), p.name(), p.height() / 10.0, "m"))
                .toList();
    }

    public List<PokemonRankingDto> getMostExperience() {
        return pokeApiClient.fetchAll().stream()
                .filter(p -> p.baseExperience() != null)
                .sorted(Comparator.comparingInt(PokeApiPokemonResponse::baseExperience).reversed())
                .limit(5)
                .map(p -> new PokemonRankingDto(p.id(), p.name(), p.baseExperience(), "pts"))
                .toList();
    }
}
