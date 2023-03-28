package io.extremum.watch.repositories;

import io.extremum.watch.config.ReactiveWatchTestConfiguration;
import io.extremum.watch.config.TestWithServices;
import io.extremum.watch.config.WatchTestConfiguration;
import io.extremum.watch.config.conditional.BlockingWatchConfiguration;
import io.extremum.watch.config.conditional.WebSocketConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@SpringBootTest(classes = {ReactiveWatchTestConfiguration.class})
@TestInstance(PER_CLASS)
class ReactiveRedisSubscriptionRepositoryTest extends TestWithServices {
    @Autowired
    private ReactiveSubscriptionRepository subscriptionRepository;

    @BeforeEach
    void cleanup() {
        subscriptionRepository.unsubscribe(Arrays.asList("dead", "beef"), "Alex")
                .and(subscriptionRepository.unsubscribe(Arrays.asList("dead", "beef"), "Ben"))
                .block();
    }

    @Test
    void givenOneSubscriptionExists_whenFindingSubscribersForThisModel_thenTheSubscriberShouldBeReturned() {
        StepVerifier.create(subscriptionRepository.subscribe(Collections.singletonList("dead"), "Alex"))
                .verifyComplete();

        StepVerifier.create(subscriptionRepository.getAllSubscribersIdsBySubscription("dead"))
                .assertNext(subscribers -> assertThat(subscribers, equalTo(Collections.singletonList("Alex"))))
                .verifyComplete();
    }

    @Test
    void givenTwoSubscriptionsExistForTwoSubscribers_whenFindingSubscribersForOneModel_thenBothSubscriberShouldBeReturned() {
        StepVerifier.create(
            subscriptionRepository.subscribe(Arrays.asList("dead", "beef"), "Alex")
                .and(subscriptionRepository.subscribe(Arrays.asList("dead", "beef"), "Ben")))
                .verifyComplete();

        StepVerifier.create(subscriptionRepository.getAllSubscribersIdsBySubscription("dead"))
                .assertNext(subscribers -> {
                    assertThat(subscribers, hasSize(2));
                    assertThat(subscribers, hasItems("Alex", "Ben"));
                })
                .verifyComplete();
    }

    @Test
    void givenTwoSubscriptionsExistForTwoSubscribers_whenAlexDeletesOneSubscription_thenForItAlexShouldNotBeFound() {
        StepVerifier.create(
            subscriptionRepository.subscribe(Arrays.asList("dead", "beef"), "Alex")
                .and(subscriptionRepository.subscribe(Arrays.asList("dead", "beef"), "Ben")))
                .verifyComplete();

        StepVerifier.create(subscriptionRepository.unsubscribe(Collections.singletonList("dead"), "Alex"))
                .verifyComplete();

        StepVerifier.create(subscriptionRepository.getAllSubscribersIdsBySubscription("dead"))
                .assertNext(subscribers -> assertThat(subscribers, equalTo(Collections.singletonList("Ben"))))
                .verifyComplete();
    }

    @Test
    void givenASubscriptionIsJustCreated_thenItShouldBeFresh() {
        Mono<Boolean> isFreshMono = subscriptionRepository.subscribe(Collections.singletonList("dead"), "Alex")
                .then(subscriptionRepository.checkFreshSubscription("dead", "Alex"));

        StepVerifier.create(isFreshMono)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void givenASubscriptionIsCreated_thenFreshCheckShouldNotSucceedTwice() {
        Mono<Boolean> isFreshMono = subscriptionRepository.subscribe(Collections.singletonList("dead"), "Alex")
                .then(subscriptionRepository.checkFreshSubscription("dead", "Alex"))
                .then(subscriptionRepository.checkFreshSubscription("dead", "Alex"));

        StepVerifier.create(isFreshMono)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void givenTwoFreshSubscriptions_thenFreshCheckOnOneShouldNotAffectTheOther() {
        Mono<Boolean> isFreshMono = subscriptionRepository.subscribe(Collections.singletonList("dead"), "Alex")
                .and(subscriptionRepository.subscribe(Collections.singletonList("dead"), "Ben"))
                .then(subscriptionRepository.checkFreshSubscription("dead", "Alex"))
                .then(subscriptionRepository.checkFreshSubscription("dead", "Ben"));

        StepVerifier.create(isFreshMono)
                .expectNext(true)
                .verifyComplete();
    }
}