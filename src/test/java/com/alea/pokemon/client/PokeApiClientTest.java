package com.alea.pokemon.client;

import com.alea.pokemon.dto.PokeApiPokemonResponse;
import com.alea.pokemon.exception.PokeApiException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class PokeApiClientTest {

    private MockWebServer mockWebServer;
    private PokeApiClient pokeApiClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();
        pokeApiClient = new PokeApiClient(webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void fetchOneHappyPathReturnsMappedPokemon() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("""
                        {"id":1,"name":"bulbasaur","weight":69,"height":7,"base_experience":64}
                        """)
                .addHeader("Content-Type", "application/json"));

        PokeApiPokemonResponse result = pokeApiClient.fetchOne("bulbasaur");

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1);
        assertThat(result.name()).isEqualTo("bulbasaur");
        assertThat(result.weight()).isEqualTo(69);
        assertThat(result.height()).isEqualTo(7);
        assertThat(result.baseExperience()).isEqualTo(64);
    }

    @Test
    void fetchOneNullBaseExperienceMapsCorrectly() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("""
                        {"id":132,"name":"ditto","weight":40,"height":3,"base_experience":null}
                        """)
                .addHeader("Content-Type", "application/json"));

        PokeApiPokemonResponse result = pokeApiClient.fetchOne("ditto");

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("ditto");
        assertThat(result.baseExperience()).isNull();
    }

    @Test
    void fetchOneNotFoundReturnsNull() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        assertThat(pokeApiClient.fetchOne("unknown")).isNull();
    }

    @Test
    void fetchAllEmptyResponseThrowsPokeApiException() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        assertThatThrownBy(() -> pokeApiClient.fetchAll())
                .isInstanceOf(PokeApiException.class);
    }

    @Test
    void fetchAllFiltersOutFailedFetches() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("""                                                                                                                                                                                                               
                      {"results":[{"name":"bulbasaur"},{"name":"unknown"}]}                                                                                                                                                              
                      """)
                .addHeader("Content-Type", "application/json"));
        mockWebServer.enqueue(new MockResponse()
                .setBody("""                                                                                                                                                                                                               
                      {"id":1,"name":"bulbasaur","weight":69,"height":7,"base_experience":64}                                                                                                                                            
                      """)
                .addHeader("Content-Type", "application/json"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        List<PokeApiPokemonResponse> result = pokeApiClient.fetchAll();

        assertThat(result).hasSize(1);
    }
}
