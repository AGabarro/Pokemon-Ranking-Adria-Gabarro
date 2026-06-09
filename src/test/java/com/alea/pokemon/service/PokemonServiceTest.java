package com.alea.pokemon.service;

import com.alea.pokemon.client.PokeApiClient;
import com.alea.pokemon.dto.PokeApiPokemonResponse;
import com.alea.pokemon.dto.PokemonRankingDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PokemonServiceTest {

    @Mock
    private PokeApiClient pokeApiClient;

    @InjectMocks
    private PokemonService pokemonService;

    @BeforeEach
    void setUp() {
        when(pokeApiClient.fetchAll()).thenReturn(buildPokemonList());
    }

    @Test
    void getHeaviestReturnsTopFiveSortedByWeightInKg() {
        List<PokemonRankingDto> result = pokemonService.getHeaviest();

        assertThat(result).hasSize(5);
        assertThat(result.get(0).value()).isEqualTo(100.0);  // 1000 hg -> 100.0 kg
        assertThat(result.get(4).value()).isEqualTo(60.0);   // 600 hg -> 60.0 kg
        assertThat(result).extracting(PokemonRankingDto::unit).containsOnly("kg");
        assertThat(result).extracting(PokemonRankingDto::value)
                .isSortedAccordingTo((a, b) -> Double.compare(b, a));
    }

    @Test
    void getHighestReturnsTopFiveSortedByHeightInMetres() {
        List<PokemonRankingDto> result = pokemonService.getHighest();

        assertThat(result).hasSize(5);
        assertThat(result.get(0).value()).isEqualTo(20.0);   // 200 dm -> 20.0 m
        assertThat(result.get(4).value()).isEqualTo(12.0);   // 120 dm -> 12.0 m
        assertThat(result).extracting(PokemonRankingDto::unit).containsOnly("m");
        assertThat(result).extracting(PokemonRankingDto::value)
                .isSortedAccordingTo((a, b) -> Double.compare(b, a));
    }

    @Test
    void getMostExperienceReturnsTopFiveSortedByBaseExperience() {
        List<PokemonRankingDto> result = pokemonService.getMostExperience();

        assertThat(result).hasSize(5);
        assertThat(result.get(0).value()).isEqualTo(300.0);
        assertThat(result).extracting(PokemonRankingDto::unit).containsOnly("pts");
        assertThat(result).extracting(PokemonRankingDto::value)
                .isSortedAccordingTo((a, b) -> Double.compare(b, a));
    }

    @Test
    void getMostExperienceFiltersOutNullBaseExperience() {
        List<PokemonRankingDto> result = pokemonService.getMostExperience();

        assertThat(result).extracting(PokemonRankingDto::name)
                .doesNotContain("pokemon-g");
    }

    @Test
    void getHeaviestWhenEmptyListReturnsEmpty() {
        when(pokeApiClient.fetchAll()).thenReturn(List.of());

        assertThat(pokemonService.getHeaviest()).isEmpty();
    }

    @Test
    void getMostExperienceWhenAllBaseExperienceNullReturnsEmpty() {
        when(pokeApiClient.fetchAll()).thenReturn(List.of(
                new PokeApiPokemonResponse(1, "pokemon-a", 1000, 200, null),
                new PokeApiPokemonResponse(2, "pokemon-b", 900, 180, null)
        ));

        assertThat(pokemonService.getMostExperience()).isEmpty();
    }

    private List<PokeApiPokemonResponse> buildPokemonList() {
        return List.of(
                new PokeApiPokemonResponse(1, "pokemon-a", 1000, 200, 300),
                new PokeApiPokemonResponse(2, "pokemon-b", 900, 180, 280),
                new PokeApiPokemonResponse(3, "pokemon-c", 800, 160, 260),
                new PokeApiPokemonResponse(4, "pokemon-d", 700, 140, 240),
                new PokeApiPokemonResponse(5, "pokemon-e", 600, 120, 220),
                new PokeApiPokemonResponse(6, "pokemon-f", 500, 100, 200),
                new PokeApiPokemonResponse(7, "pokemon-g", 400, 80, null),
                new PokeApiPokemonResponse(8, "pokemon-h", 300, 60, 160),
                new PokeApiPokemonResponse(9, "pokemon-i", 200, 40, 140),
                new PokeApiPokemonResponse(10, "pokemon-j", 100, 20, 120)
        );
    }
}
