package io.extremum.watch.services;

import io.extremum.common.support.UniversalModelFinder;
import io.extremum.security.DataSecurity;
import io.extremum.security.ExtremumAccessDeniedException;
import io.extremum.security.RoleSecurity;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.watch.end2end.fixture.WatchedModel;
import io.extremum.watch.repositories.SubscriptionRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class PersistentWatchSubscriptionServiceTest {
    @InjectMocks
    private PersistentWatchSubscriptionService service;

    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private RoleSecurity roleSecurity;
    @Mock
    private DataSecurity dataSecurity;
    @Mock
    private UniversalModelFinder universalModelFinder;

    @Test
    void whenSubscribing_thenAllSubscriptionsShouldBeAddedToTheRepository() {
        service.subscribe(Arrays.asList(fromInternalId("dead"), fromInternalId("beef")), "Alex");

        verify(subscriptionRepository).subscribe(Arrays.asList("dead", "beef"), "Alex");
    }
    
    private Descriptor fromInternalId(String internalId) {
        return Descriptor.builder()
                .internalId(internalId)
                .build();
    }

    @Test
    void whenUnsubscribing_thenAllSubscriptionsShouldBeRemovedFromTheRepository() {
        service.unsubscribe(Arrays.asList(fromInternalId("dead"), fromInternalId("beef")), "Alex");

        verify(subscriptionRepository).unsubscribe(Arrays.asList("dead", "beef"), "Alex");
    }

    @Test
    void whenFindingSubscriptionsBySubscriber_thenRepositoryShouldBeConsulted() {
        when(subscriptionRepository.getAllSubscribersIdsBySubscription("beef"))
                .thenReturn(Arrays.asList("Alex", "Ben"));

        Collection<String> subscribers = service.findAllSubscribersBySubscription("beef");
        assertThat(subscribers, Matchers.containsInAnyOrder("Alex", "Ben"));
    }

    @Test
    void whenRoleSecurityDoesNotAllowToWatch_thenADeniedExceptionShouldBeThrown() {
        doThrow(new ExtremumAccessDeniedException("Not allowed to watch")).when(roleSecurity).checkWatchAllowed(any());

        subscribeAndExpectToBeDeniedAccess();
    }

    private void subscribeAndExpectToBeDeniedAccess() {
        try {
            service.subscribe(Collections.singleton(fromInternalId("dead")), "Alex");
            fail("An exception should be thrown");
        } catch (ExtremumAccessDeniedException e) {
            assertThat(e.getMessage(), is("Not allowed to watch"));
        }
    }

    @Test
    void whenDataSecurityDoesNotAllowToWatch_thenADeniedExceptionShouldBeThrown() {
        when(universalModelFinder.findModels(any())).thenReturn(singletonList(new WatchedModel()));
        doThrow(new ExtremumAccessDeniedException("Not allowed to watch")).when(dataSecurity).checkWatchAllowed(any());

        subscribeAndExpectToBeDeniedAccess();
    }
}