package io.extremum.watch.processor;

import io.extremum.watch.models.TextWatchEvent;
import io.extremum.watch.services.TextWatchEventService;
import io.extremum.watch.services.WatchSubscriptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Collections.singleton;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class WatchEventDispatcherTest {
    @InjectMocks
    private WatchEventDispatcher dispatcher;

    @Mock
    private TextWatchEventService textWatchEventService;
    @Mock
    private WatchSubscriptionService watchSubscriptionService;
    @Mock
    private WatchEventNotificationSender notificationSender;

    @Test
    void whenAnEventIsDispatched_thenItShouldBeSavedWithItsSubscribersAndANotificationSendShouldBeTriggered() {
        String modelId = "the-id";
        TextWatchEvent event = new TextWatchEvent("the-patch", "the-full-patch", modelId, new WatchedModel());
        when(watchSubscriptionService.findAllSubscribersBySubscription("the-id"))
                .thenReturn(singleton("Alex"));

        dispatcher.consume(event);

        assertThat(event.getSubscribers(), is(equalTo(singleton("Alex"))));
        verify(textWatchEventService).save(event);
        verify(notificationSender).send(anyString(), any());
    }
}