package io.extremum.rdf.triple.controller;

import io.extremum.rdf.triple.configuration.TestConfiguration;
import io.extremum.rdf.triple.controller.dto.TripleDto;
import io.extremum.rdf.triple.controller.dto.TripleResponseDto;
import io.extremum.rdf.triple.dao.mongo.model.Triple;
import io.extremum.rdf.triple.service.TripleService;
import io.extremum.rdf.triple.service.converter.TripleConverter;
import io.extremum.sharedmodels.basic.StringOrObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(SpringExtension.class)
@WebFluxTest(value = TripleController.class)
@ContextConfiguration(classes = TestConfiguration.class)
class TripleControllerTest {

    private WebTestClient webClient;

    @Mock
    private TripleService service;

    @Mock
    private TripleConverter converter;

    @BeforeEach
    void initClient() {
        webClient = WebTestClient.bindToController(new TripleController(service, converter)).build();
    }

    @Test
    void should_get_links() {
        when(service.getLinks("testSubject1", "testPredicate1", 1, 1)).thenReturn(
                Flux.fromIterable(
                        Collections.singletonList(
                                new Triple(
                                        "testSubject",
                                        "testPredicate",
                                        Arrays.asList("obj1", "obj2")
                                )
                        )
                )
        );
        when(converter.convertToResponseReactively(any(), any())).thenReturn(
                Mono.just(
                        new TripleResponseDto(
                                "testSubject1",
                                "testPredicate1",
                                Arrays.asList(new StringOrObject<>("obj1"), new StringOrObject<>("obj2"))
                        )
                )
        );

        webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/links")
                        .queryParam("limit", "1")
                        .queryParam("offset", "1")
                        .queryParam("subject", "testSubject1")
                        .queryParam("predicate", "testPredicate1")
                        .build()
                )
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.result[0].predicate").isEqualTo("testPredicate1")
                .jsonPath("$.result[0].subject").isEqualTo("testSubject1")
                .jsonPath("$.result[0].objects").isArray();
    }

    @Test
    void should_update_links() {
        TripleDto tripleDto = new TripleDto("testSubject", "testPredicate", "https://example.com/genid/9768324f-7291-4ddc-b18e-ae2f6f152cf5", false);
        when(service.createOrUpdate(tripleDto))
                .thenReturn(Mono.just(true));

        webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/links").build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(
                        "{\n" +
                                "    \"subject\": \"testSubject\",\n" +
                                "    \"predicate\": \"testPredicate\",\n" +
                                "    \"object\": \"https://example.com/genid/9768324f-7291-4ddc-b18e-ae2f6f152cf5\"\n" +
                                "}"
                )
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody();

        verify(service, times(1)).createOrUpdate(tripleDto);

        tripleDto = new TripleDto("testSubject", "testPredicate", "https://example.com/genid/9768324f-7291-4ddc-b18e-ae2f6f152cf5", true);
        when(service.delete(tripleDto))
                .thenReturn(Mono.just(true));
        webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/links").build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(
                        "{\n" +
                                "    \"subject\": \"testSubject\",\n" +
                                "    \"predicate\": \"testPredicate\",\n" +
                                "    \"object\": \"https://example.com/genid/9768324f-7291-4ddc-b18e-ae2f6f152cf5\",\n" +
                                "    \"delete\": true\n" +
                                "}"
                )
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody();

        verify(service, times(1)).delete(tripleDto);
    }
}