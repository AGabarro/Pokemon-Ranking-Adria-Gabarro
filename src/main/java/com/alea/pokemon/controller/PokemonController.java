package com.alea.pokemon.controller;

import com.alea.pokemon.dto.PokemonRankingDto;
import com.alea.pokemon.service.PokemonService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/pokemon")
public class PokemonController {

    private final PokemonService pokemonService;

    public PokemonController(PokemonService pokemonService) {
        this.pokemonService = pokemonService;
    }

    @GetMapping("/heaviest")
    public List<PokemonRankingDto> getHeaviest() {
        return pokemonService.getHeaviest();
    }

    @GetMapping("/tallest")
    public List<PokemonRankingDto> getTallest() {
        return pokemonService.getHighest();
    }

    @GetMapping("/most-experienced")
    public List<PokemonRankingDto> getMostExperienced() {
        return pokemonService.getMostExperience();
    }
}
