package io.extremum.watch.processor;

import io.extremum.watch.config.conditional.BlockingWatchConfiguration;
import io.extremum.watch.models.TextWatchEvent;
import io.extremum.watch.services.TextWatchEventService;
import io.extremum.watch.services.WatchSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Service
@ConditionalOnBean(BlockingWatchConfiguration.class)
public final class WatchEventDispatcher implements WatchEventConsumer {
    private final TextWatchEventService textWatchEventService;
    private final WatchSubscriptionService watchSubscriptionService;
    private final WatchEventNotificationSender notificationSender;

    @Override
    public void consume(TextWatchEvent event) {
        Collection<String> subscribers = watchSubscriptionService.findAllSubscribersBySubscription(event.getModelId());

        event.setSubscribers(collectionToSet(subscribers));
        textWatchEventService.save(event);

        notificationSender.send(event.getModelId(), event.toDto());
    }

    private Set<String> collectionToSet(Collection<String> subscribers) {
        Set<String> set = new HashSet<>(subscribers);
        return Collections.unmodifiableSet(set);
    }
}
