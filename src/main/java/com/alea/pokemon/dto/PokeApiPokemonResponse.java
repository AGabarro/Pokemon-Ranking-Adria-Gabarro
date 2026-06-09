package com.alea.pokemon.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PokeApiPokemonResponse(
        int id,
        String name,
        int weight,
        int height,
        @JsonProperty("base_experience") Integer baseExperience
) {}
