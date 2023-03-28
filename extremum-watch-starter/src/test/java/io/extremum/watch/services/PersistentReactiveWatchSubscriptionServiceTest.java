package io.extremum.watch.services;

import io.extremum.common.support.UniversalReactiveModelLoaders;
import io.extremum.security.*;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.watch.end2end.fixture.WatchedModel;
import io.extremum.watch.repositories.ReactiveSubscriptionRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PersistentReactiveWatchSubscriptionServiceTest {
    @InjectMocks
    private PersistentReactiveWatchSubscriptionService service;

    @Mock
    private ReactiveSubscriptionRepository subscriptionRepository;
    @Mock
    private ReactiveRoleSecurity roleSecurity;
    @Mock
    private ReactiveDataSecurity dataSecurity;
    @Mock
    private UniversalReactiveModelLoaders universalReactiveModelLoaders;

    private void doCommonStubbing(boolean roleSecurityAllowsWatch, boolean dataSecurityAllowsWatch) {
        when(universalReactiveModelLoaders.loadByDescriptor(any())).thenReturn(Mono.just(new WatchedModel()));
        when(roleSecurity.checkWatchAllowed(any())).thenReturn(
                roleSecurityAllowsWatch ?
                        Mono.empty() :
                        Mono.error(new ExtremumAccessDeniedException("Not allowed to watch")));
        when(dataSecurity.checkWatchAllowed(any())).thenReturn(
                dataSecurityAllowsWatch ?
                        Mono.empty() :
                        Mono.error(new ExtremumAccessDeniedException("Not allowed to watch")));
    }

    @Test
    void whenSubscribing_thenAllSubscriptionsShouldBeAddedToTheRepository() {
        doCommonStubbing(true, true);
        when(subscriptionRepository.subscribe(any(), any())).thenReturn(Mono.empty());

        StepVerifier.create(service.subscribe(Arrays.asList(fromInternalId("dead"), fromInternalId("beef")), "Alex"))
                .then(() -> verify(subscriptionRepository).subscribe(Arrays.asList("dead", "beef"), "Alex"))
                .verifyComplete();
    }

    private Descriptor fromInternalId(String internalId) {
        return Descriptor.builder()
                .internalId(internalId)
                .build();
    }

    @Test
    void whenUnsubscribing_thenAllSubscriptionsShouldBeRemovedFromTheRepository() {
        when(subscriptionRepository.unsubscribe(any(), any())).thenReturn(Mono.empty());

        StepVerifier.create(service.unsubscribe(Arrays.asList(fromInternalId("dead"), fromInternalId("beef")), "Alex"))
                .then(() -> verify(subscriptionRepository).unsubscribe(Arrays.asList("dead", "beef"), "Alex"))
                .verifyComplete();
    }

    @Test
    void whenFindingSubscriptionsBySubscriber_thenRepositoryShouldBeConsulted() {
        when(subscriptionRepository.getAllSubscribersIdsBySubscription("beef"))
                .thenReturn(Mono.just(Arrays.asList("Alex", "Ben")));

        StepVerifier.create(service.findAllSubscribersBySubscription("beef"))
                .assertNext(subscribers -> assertThat(subscribers, Matchers.containsInAnyOrder("Alex", "Ben")))
                .verifyComplete();
    }

    @Test
    void whenRoleSecurityDoesNotAllowToWatch_thenADeniedExceptionShouldBeThrown() {
        doCommonStubbing(false, true);
        subscribeAndExpectToBeDeniedAccess();
    }

    private void subscribeAndExpectToBeDeniedAccess() {
        StepVerifier.create(service.subscribe(Collections.singleton(fromInternalId("dead")), "Alex"))
                .verifyError(ExtremumAccessDeniedException.class);
    }

    @Test
    void whenDataSecurityDoesNotAllowToWatch_thenADeniedExceptionShouldBeThrown() {
        doCommonStubbing(true, false);
        subscribeAndExpectToBeDeniedAccess();
    }
}