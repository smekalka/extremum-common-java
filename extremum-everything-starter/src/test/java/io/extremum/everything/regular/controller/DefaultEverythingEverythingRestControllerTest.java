package io.extremum.everything.regular.controller;

import io.extremum.everything.services.management.EverythingCollectionManagementService;
import io.extremum.everything.services.management.EverythingEverythingManagementService;
import io.extremum.everything.services.management.EverythingGetDemultiplexerOnDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = EverythingControllersTestConfiguration.class)
class DefaultEverythingEverythingRestControllerTest {
    private WebTestClient webClient;

    @MockBean
    private EverythingCollectionManagementService collectionManagementService;
    @MockBean
    private EverythingEverythingManagementService everythingEverythingManagementService;

    @BeforeEach
    void initClient() {
        Object controller = new DefaultEverythingEverythingRestController(
                everythingEverythingManagementService, collectionManagementService,
                new EverythingGetDemultiplexerOnDescriptor(everythingEverythingManagementService, collectionManagementService));
        webClient = WebTestClient.bindToController(controller).build();
    }

    @Test
    void streamsCollection() {
        String randomUuid = UUID.randomUUID().toString();
        when(collectionManagementService.streamCollection(eq(randomUuid), any(), anyBoolean()))
                .thenReturn(Flux.just(new TestResponseDto("first"), new TestResponseDto("second")));

        List<TestResponseDto> dtos = webClient.get()
                .uri("/" + randomUuid)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(TestResponseDto.class)
                .returnResult()
                .getResponseBody();

        assertThatFirstAndSecondAreReturned(dtos);
    }

    private void assertThatFirstAndSecondAreReturned(List<TestResponseDto> dtos) {
        assertThat(dtos, hasSize(2));
        assertThat(dtos.get(0).name, is("first"));
        assertThat(dtos.get(1).name, is("second"));
    }

    @Test
    void whenAnExceptionOccursDuringStreaming_thenItShouldBeHandled() {
        String randomUuid = UUID.randomUUID().toString();
        when(collectionManagementService.streamCollection(eq(randomUuid), any(), anyBoolean()))
                .thenReturn(Flux.error(new RuntimeException("Oops!")));

        String responseText = webClient.get().uri("/" + randomUuid)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        assertThat(responseText, startsWith("event:internal-error\ndata:Internal error "));
    }
}