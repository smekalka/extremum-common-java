package io.extremum.everything.reactive.controller;

import com.fasterxml.jackson.databind.node.TextNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jackson.jsonpointer.JsonPointerException;
import com.github.fge.jsonpatch.AddOperation;
import com.github.fge.jsonpatch.JsonPatch;
import io.extremum.common.descriptor.service.ReactiveDescriptorService;
import io.extremum.everything.services.management.EverythingCollectionManagementService;
import io.extremum.everything.services.management.ReactiveEverythingManagementService;
import io.extremum.everything.services.management.ReactiveGetDemultiplexer;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.Response;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.extremum.test.core.ResponseMatchers.notFound;
import static io.extremum.test.core.ResponseMatchers.successful;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = EverythingControllersTestConfiguration.class)
class ReactiveEverythingEverythingRestControllerTest {
    private WebTestClient webClient;

    @MockBean
    private EverythingCollectionManagementService collectionManagementService;
    @MockBean
    private ReactiveEverythingManagementService everythingEverythingManagementService;
    @MockBean
    private ReactiveGetDemultiplexer demultiplexer;
    @MockBean
    private ReactiveDescriptorService reactiveDescriptorService;

    @BeforeEach
    void initClient() {
        Object controller = new ReactiveEverythingEverythingRestController(
                everythingEverythingManagementService, collectionManagementService,
                demultiplexer, reactiveDescriptorService);
        webClient = WebTestClient.bindToController(controller).build();
    }

    @Test
    void gets() {
        when(demultiplexer.get(any(), any(), anyBoolean()))
                .thenReturn(Mono.just(Response.ok(standardTestResponseDto())));

        Response response = getViaEverythingGet(randomUuid());

        assertThatStandardTestResponseIsCorrect(response);
    }

    @Test
    void get_by_iri() {
        when(demultiplexer.get(any(), any(), anyBoolean()))
                .thenReturn(Mono.just(Response.ok(standardTestResponseDto())));

        when(reactiveDescriptorService.loadByIri(any()))
                .thenReturn(Mono.just(Descriptor.builder().externalId("external-id").internalId("internal-id").build()));

        Response response = getViaEverythingGet("a/b/c/");

        assertThatStandardTestResponseIsCorrect(response);
    }

    private void assertThatStandardTestResponseIsCorrect(Response response) {
        assertThat(response, is(successful()));

        assertThat(response.getResult(), is(notNullValue()));
        @SuppressWarnings("unchecked") Map<String, Object> result = (Map<String, Object>) response.getResult();
        assertThat(result, hasEntry("name", "test"));
    }

    @NotNull
    private TestResponseDto standardTestResponseDto() {
        return new TestResponseDto("test");
    }

    @NotNull
    private String randomUuid() {
        return UUID.randomUUID().toString();
    }

    @NotNull
    private Response getViaEverythingGet(String hierPart) {
        Response response = webClient.get()
                .uri("/" + hierPart)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Response.class)
                .returnResult()
                .getResponseBody();

        assertThat(response, is(notNullValue()));
        return response;
    }

    @Test
    void patches() throws JsonPointerException {
        when(everythingEverythingManagementService.patch(any(), any(), anyBoolean()))
                .thenReturn(Mono.just(standardTestResponseDto()));

        Response response = webClient.patch()
                .uri("/" + randomUuid())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(jsonPatch()))
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Response.class)
                .returnResult()
                .getResponseBody();
        assertThat(response, is(notNullValue()));

        assertThatStandardTestResponseIsCorrect(response);

        //noinspection UnassignedFluxMonoInstance
        verify(everythingEverythingManagementService).patch(any(), any(), anyBoolean());
    }

    @Test
    void patches_by_iri() throws JsonPointerException {
        when(everythingEverythingManagementService.patch(any(), any(), anyBoolean()))
                .thenReturn(Mono.just(standardTestResponseDto()));

        when(reactiveDescriptorService.loadByIri(any()))
                .thenReturn(Mono.just(Descriptor.builder().externalId("external-id").internalId("internal-id").build()));

        String hierPart = "a/b/c";
        Response response = webClient.patch()
                .uri("/" + hierPart)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(jsonPatch()))
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Response.class)
                .returnResult()
                .getResponseBody();
        assertThat(response, is(notNullValue()));

        assertThatStandardTestResponseIsCorrect(response);

        //noinspection UnassignedFluxMonoInstance
        verify(everythingEverythingManagementService).patch(any(), any(), anyBoolean());
    }

    @NotNull
    private JsonPatch jsonPatch() throws JsonPointerException {
        AddOperation operation = new AddOperation(new JsonPointer("/name"), new TextNode("new test"));
        return new JsonPatch(singletonList(operation));
    }

    @Test
    void deletes() {
        String externalId = randomUuid();
        when(everythingEverythingManagementService.remove(any()))
                .thenReturn(Mono.empty());

        Response response = webClient.delete()
                .uri("/" + externalId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Response.class)
                .returnResult()
                .getResponseBody();

        assertThat(response, is(notNullValue()));
        assertThat(response, is(successful()));
        assertThat(response.getResult(), is(nullValue()));

        //noinspection UnassignedFluxMonoInstance
        verify(everythingEverythingManagementService).remove(any());
    }

    @Test
    void deletes_by_iri() {
        when(reactiveDescriptorService.loadByIri(any()))
                .thenReturn(Mono.just(Descriptor.builder().externalId("external-id").internalId("internal-id").build()));

        String hierPart = "a/b/c";
        when(everythingEverythingManagementService.remove(any()))
                .thenReturn(Mono.empty());

        Response response = webClient.delete()
                .uri("/" + hierPart)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Response.class)
                .returnResult()
                .getResponseBody();

        assertThat(response, is(notNullValue()));
        assertThat(response, is(successful()));
        assertThat(response.getResult(), is(nullValue()));

        //noinspection UnassignedFluxMonoInstance
        verify(everythingEverythingManagementService).remove(any());
    }

    @Test
    void streamsCollection() {
        String randomUuid = randomUuid();
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
        String randomUuid = randomUuid();
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

    @Test
    void givenNoSuchDescriptorExists_whenGetting_then404ShouldBeReturned() {
        when(demultiplexer.get(anyString(), any(), anyBoolean())).thenReturn(Mono.empty());

        Response response = getViaEverythingGet(UUID.randomUUID().toString());

        assertThat(response, is(notFound()));
    }
}