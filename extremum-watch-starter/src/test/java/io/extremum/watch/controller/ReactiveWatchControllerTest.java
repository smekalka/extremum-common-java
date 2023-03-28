package io.extremum.watch.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jackson.jsonpointer.JsonPointerException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchOperation;
import com.github.fge.jsonpatch.ReplaceOperation;
import com.jayway.jsonpath.JsonPath;
import io.extremum.common.mapper.MapperDependencies;
import io.extremum.common.mapper.SystemJsonObjectMapper;
import io.extremum.datetime.ApiDateTimeFormat;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.watch.config.conditional.ReactiveWatchConfiguration;
import io.extremum.watch.models.TextWatchEvent;
import io.extremum.watch.processor.StompHandler;
import io.extremum.watch.services.ReactiveWatchEventService;
import io.extremum.watch.services.ReactiveWatchSubscriberIdProvider;
import io.extremum.watch.services.ReactiveWatchSubscriptionService;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.extremum.test.core.StringResponseMatchers.successfulResponse;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebFluxTest(value = ReactiveWatchController.class, properties = {"custom.watch.reactive=true"})
@ContextConfiguration(classes = {
        WatchControllersTestConfiguration.class,
        ReactiveWatchConfiguration.class,
        StompHandler.class,
        ReactiveWatchController.class
})
class ReactiveWatchControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    private final ObjectMapper clientMapper = new SystemJsonObjectMapper(Mockito.mock(MapperDependencies.class));

    @MockBean
    private ReactiveWatchEventService watchEventService;
    @MockBean
    private ReactiveWatchSubscriberIdProvider subscriberIdProvider;
    @MockBean
    private ReactiveWatchSubscriptionService watchSubscriptionService;

    @Captor
    private ArgumentCaptor<Collection<Descriptor>> descriptorsCaptor;

    private final ApiDateTimeFormat apiDateTimeFormat = new ApiDateTimeFormat();

    @Test
    void whenPuttingTwoDescriptorsToWatchList_thenBothShouldBeAdded() throws Exception {
        when(subscriberIdProvider.getSubscriberId()).thenReturn(Mono.just("Alex"));
        when(watchSubscriptionService.subscribe(any(), any())).thenReturn(Mono.empty());

        Flux<String> responseFlux = webTestClient.put()
                .uri("/watch")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("[\"dead\",\"beef\"]")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .returnResult(String.class)
                .getResponseBody();

        StepVerifier.create(responseFlux)
                .assertNext(response -> {
                    assertThat(response, is(successfulResponse()));
                    verify(watchSubscriptionService).subscribe(descriptorsCaptor.capture(), eq("Alex"));
                    Collection<Descriptor> savedDescriptors = descriptorsCaptor.getValue();
                    assertThat(savedDescriptors, containsInAnyOrder(withExternalId("dead"), withExternalId("beef")));
                })
                .verifyComplete();
    }

    private Matcher<Descriptor> withExternalId(String externalId) {
        return Matchers.hasProperty("externalId", equalTo(externalId));
    }

    @Test
    void givenOneEventExists_whenGettingTheEventWithoutFiltration_thenItShouldBeReturned() throws Exception {
        when(subscriberIdProvider.getSubscriberId()).thenReturn(Mono.just("Alex"));
        when(watchEventService.findEvents("Alex", Optional.empty(), Optional.empty(), Optional.empty()))
                .thenReturn(singleEventForReplaceFieldToNewValue());

        Flux<String> responseFlux = webTestClient.get()
                .uri("/watch")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .returnResult(String.class)
                .getResponseBody();

        StepVerifier.create(responseFlux)
                .assertNext(response -> {
                    List<Map<String, Object>> events = parseEvents(response);
                    assertThatTheEventIsAsExpected(events);
                })
                .verifyComplete();
    }

    @Test
    void givenOneEventExists_whenGettingTheEventWithSinceUntil_thenItShouldBeReturned() throws Exception {
        ZonedDateTime since = ZonedDateTime.now().minusDays(1);
        ZonedDateTime until = since.plusDays(2);

        when(subscriberIdProvider.getSubscriberId()).thenReturn(Mono.just("Alex"));
        when(watchEventService.findEvents(eq("Alex"), any(), any(), eq(Optional.of(10))))
                .thenReturn(singleEventForReplaceFieldToNewValue());

        Flux<String> responseFlux = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/watch")
                        .queryParam("since", "{since}")
                        .queryParam("until", "{until}")
                        .queryParam("limit", "{limit}")
                        .build(apiDateTimeFormat.format(since),
                                apiDateTimeFormat.format(until),
                                "10"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .returnResult(String.class)
                .getResponseBody();

        StepVerifier.create(responseFlux)
                .assertNext(response -> {
                    List<Map<String, Object>> events = parseEvents(response);
                    assertThatTheEventIsAsExpected(events);
                })
                .verifyComplete();
    }

    @NotNull
    private Flux<TextWatchEvent> singleEventForReplaceFieldToNewValue() throws JsonPointerException, JsonProcessingException {
        JsonPatchOperation operation = new ReplaceOperation(new JsonPointer("/field"), new TextNode("new-value"));
        JsonPatch jsonPatch = new JsonPatch(Collections.singletonList(operation));
        String patchAsString = clientMapper.writeValueAsString(jsonPatch);
        return Flux.just(
                new TextWatchEvent(patchAsString, null, "internalId", new ModelWithFilledValues()));

    }

    private void assertThatTheEventIsAsExpected(List<Map<String, Object>> events) {
        assertThat(events, hasSize(1));
        Map<String, Object> event = events.get(0);

        @SuppressWarnings("unchecked")
        Map<String, Object> object = (Map<String, Object>) event.get("object");
        assertThat(object, is(notNullValue()));
        assertThat(object.get("id"), is(notNullValue()));
        assertThat(object.get("model"), is("ModelWithExpectedValues"));
        assertThat(object.get("created"), is(notNullValue()));
        assertThat(object.get("modified"), is(notNullValue()));
        assertThat(object.get("version"), is(1));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> operations = (List<Map<String, Object>>) event.get("patch");
        assertThat(operations, is(notNullValue()));
        assertThat(operations, hasSize(1));
        Map<String, Object> patchOperation = operations.get(0);
        assertThat(patchOperation.get("path"), is("/field"));
        assertThat(patchOperation.get("op"), is("replace"));
        assertThat(patchOperation.get("value"), is("new-value"));
    }

    private List<Map<String, Object>> parseEvents(String response) {
        return JsonPath.parse(response).read("$");
    }

    @Test
    void whenDeletingTwoDescriptorsFromWatchList_thenBothShouldBeRemoved() throws Exception {
        when(subscriberIdProvider.getSubscriberId()).thenReturn(Mono.just("Alex"));
        when(watchSubscriptionService.unsubscribe(any(), any())).thenReturn(Mono.empty());

        Flux<String> responseFlux = webTestClient
                .method(HttpMethod.DELETE)
                .uri("/watch")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("[\"dead\",\"beef\"]")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .returnResult(String.class)
                .getResponseBody();

        StepVerifier.create(responseFlux)
                .assertNext(response -> {
                    assertThat(response, is(successfulResponse()));
                    verify(watchSubscriptionService).unsubscribe(descriptorsCaptor.capture(), eq("Alex"));
                    Collection<Descriptor> removedDescriptors = descriptorsCaptor.getValue();
                    assertThat(removedDescriptors, containsInAnyOrder(withExternalId("dead"), withExternalId("beef")));
                })
                .verifyComplete();
    }
}
