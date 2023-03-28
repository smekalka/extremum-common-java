package io.extremum.watch.repositories;

import io.extremum.common.spring.data.OffsetBasedPageRequest;
import io.extremum.watch.config.ReactiveWatchTestConfiguration;
import io.extremum.watch.config.TestWithServices;
import io.extremum.watch.config.conditional.ReactiveWatchConfiguration;
import io.extremum.watch.controller.ModelWithFilledValues;
import io.extremum.watch.models.TextWatchEvent;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.UUID;

import static io.extremum.mongo.MongoConstants.DISTANT_FUTURE;
import static io.extremum.mongo.MongoConstants.DISTANT_PAST;

@SpringBootTest(
        classes = {ReactiveWatchTestConfiguration.class, ReactiveWatchConfiguration.class},
        properties = {"spring.main.web-application-type=reactive", "custom.watch.reactive=true"})
class ReactiveTextWatchEventRepositoryTest extends TestWithServices {
    @Autowired
    private ReactiveTextWatchEventRepository repository;

    private final String subscriber = randomString();

    @NotNull
    private String randomString() {
        return UUID.randomUUID().toString();
    }

    @Test
    void whenSearchingBySubscriberInSubscribers_thenShouldFindSomething() {
        StepVerifier.create(saveAnEventVisibleFor(subscriber)).expectNextCount(1).verifyComplete();

        StepVerifier.create(repository.findBySubscribersAndCreatedBetweenOrderByCreatedAscIdAsc(
                    subscriber, DISTANT_PAST, DISTANT_FUTURE, Pageable.unpaged()))
                .expectNextCount(1)
                .verifyComplete();
    }

    private Mono<TextWatchEvent> saveAnEventVisibleFor(String subscriber) {
        String modelId = randomString();
        TextWatchEvent event = new TextWatchEvent("patch", "full-patch", modelId, new ModelWithFilledValues());
        event.setSubscribers(Collections.singleton(subscriber));
        return repository.save(event);
    }

    @Test
    void whenSearchingBySubscriberNotInSubscribers_thenShouldNotFindAnything() {
        StepVerifier.create(saveAnEventVisibleFor(subscriber)).expectNextCount(1).verifyComplete();

        String anotherSubscriber = randomString();
        StepVerifier.create(repository.findBySubscribersAndCreatedBetweenOrderByCreatedAscIdAsc(
                anotherSubscriber, DISTANT_PAST, DISTANT_FUTURE, Pageable.unpaged()))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void whenSearchingUntilYesterday_thenShouldNotFoundAnything() {
        StepVerifier.create(saveAnEventVisibleFor(subscriber)).expectNextCount(1).verifyComplete();

        StepVerifier.create(repository.findBySubscribersAndCreatedBetweenOrderByCreatedAscIdAsc(
                subscriber, DISTANT_PAST, yesterday(), Pageable.unpaged()))
                .expectNextCount(0)
                .verifyComplete();
        saveAnEventVisibleFor(subscriber);
    }

    @NotNull
    private ZonedDateTime yesterday() {
        return ZonedDateTime.now().minusDays(1);
    }

    @Test
    void whenSearchingFromTomorrow_thenShouldNotFoundAnything() {
        StepVerifier.create(saveAnEventVisibleFor(subscriber)).expectNextCount(1).verifyComplete();

        StepVerifier.create(repository.findBySubscribersAndCreatedBetweenOrderByCreatedAscIdAsc(
                subscriber, tomorrow(), DISTANT_FUTURE, Pageable.unpaged()))
                .expectNextCount(0)
                .verifyComplete();
        saveAnEventVisibleFor(subscriber);
    }

    @Test
    void given2EventsExist_whenSearchingWithLimit1_thenShouldOnlyFind1() {
        StepVerifier.create(saveAnEventVisibleFor(subscriber).zipWith(saveAnEventVisibleFor(subscriber)))
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier.create(repository.findBySubscribersAndCreatedBetweenOrderByCreatedAscIdAsc(
                subscriber, DISTANT_PAST, DISTANT_FUTURE, OffsetBasedPageRequest.limit(1)))
                .expectNextCount(1)
                .verifyComplete();
        saveAnEventVisibleFor(subscriber);
    }

    @NotNull
    private ZonedDateTime tomorrow() {
        return ZonedDateTime.now().plusDays(1);
    }
}