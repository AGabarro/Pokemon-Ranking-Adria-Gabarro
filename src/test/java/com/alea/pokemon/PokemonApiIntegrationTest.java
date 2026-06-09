package com.alea.pokemon;

import com.alea.pokemon.client.PokeApiClient;
import com.alea.pokemon.dto.PokeApiPokemonResponse;
import com.alea.pokemon.dto.PokemonRankingDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PokemonApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    private PokeApiClient pokeApiClient;

    @Test
    void allRankingEndpointsReturnTopFiveCorrectly() {
        when(pokeApiClient.fetchAll()).thenReturn(List.of(
                new PokeApiPokemonResponse(143, "snorlax",   4600, 21, 189),
                new PokeApiPokemonResponse(150, "mewtwo",    1220, 20, 306),
                new PokeApiPokemonResponse(6,   "charizard",  905, 17, 267),
                new PokeApiPokemonResponse(130, "gyarados",  2350, 65, 189),
                new PokeApiPokemonResponse(149, "dragonite", 2100, 22, 270),
                new PokeApiPokemonResponse(131, "lapras",    2200, 25, 187)
        ));

        ResponseEntity<List<PokemonRankingDto>> heaviest = get("/pokemon/heaviest");
        assertThat(heaviest.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(heaviest.getBody()).hasSize(5);
        assertThat(heaviest.getBody().getFirst().name()).isEqualTo("snorlax");
        assertThat(heaviest.getBody().getFirst().value()).isEqualTo(460.0);
        assertThat(heaviest.getBody().getFirst().unit()).isEqualTo("kg");

        ResponseEntity<List<PokemonRankingDto>> tallest = get("/pokemon/tallest");
        assertThat(tallest.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(tallest.getBody()).hasSize(5);
        assertThat(tallest.getBody().getFirst().name()).isEqualTo("gyarados");
        assertThat(tallest.getBody().getFirst().unit()).isEqualTo("m");

        ResponseEntity<List<PokemonRankingDto>> experienced = get("/pokemon/most-experienced");
        assertThat(experienced.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(experienced.getBody()).hasSize(5);
        assertThat(experienced.getBody().getFirst().name()).isEqualTo("mewtwo");
        assertThat(experienced.getBody().getFirst().unit()).isEqualTo("pts");
    }

    private ResponseEntity<List<PokemonRankingDto>> get(String path) {
        return restTemplate.exchange(path, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
    }
}
