package io.extremum.watch.processor;

import io.extremum.watch.models.TextWatchEvent;
import io.extremum.watch.repositories.ReactiveTextWatchEventRepository;
import io.extremum.watch.services.ReactiveWatchSubscriptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Collections;

import static java.util.Collections.singleton;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReactiveWatchEventDispatcherTest {
    @InjectMocks
    private ReactiveWatchEventDispatcher dispatcher;

    @Mock
    private ReactiveTextWatchEventRepository eventRepository;
    @Mock
    private ReactiveWatchSubscriptionService watchSubscriptionService;
    @Mock
    private ReactiveWatchEventNotificationSender notificationSender;

    @Test
    void whenAnEventIsDispatched_thenItShouldBeSavedWithItsSubscribersAndANotificationSendShouldBeTriggered() {
        this.dispatcher.setSenders(Collections.singletonList(notificationSender));
        String modelId = "the-id";
        TextWatchEvent event = new TextWatchEvent("the-patch", "the-full-patch", modelId, new WatchedModel());
        when(watchSubscriptionService.findAllSubscribersBySubscription("the-id"))
                .thenReturn(Mono.just(singleton("Alex")));
        when(eventRepository.save(any())).thenReturn(Mono.just(event));
        when(notificationSender.send(anyString(), any())).thenReturn(Mono.empty());

        StepVerifier.create(dispatcher.consume(event))
                .thenAwait(Duration.ofMillis(100))
                .then(() -> {
                    assertThat(event.getSubscribers(), is(equalTo(singleton("Alex"))));
                    verify(eventRepository).save(event);
                    verify(notificationSender).send(anyString(), any());
                })
                .verifyComplete();
    }
}