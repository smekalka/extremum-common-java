package io.extremum.watch.processor;

import io.extremum.watch.config.conditional.ReactiveWatchConfiguration;
import io.extremum.watch.models.TextWatchEvent;
import io.extremum.watch.repositories.ReactiveTextWatchEventRepository;
import io.extremum.watch.services.ReactiveWatchSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@RequiredArgsConstructor
@Service
@ConditionalOnBean(ReactiveWatchConfiguration.class)
public final class ReactiveWatchEventDispatcher implements ReactiveWatchEventConsumer {
    private final ReactiveTextWatchEventRepository eventRepository;
    private final ReactiveWatchSubscriptionService watchSubscriptionService;

    private List<ReactiveWatchEventNotificationSender> notificationSenders = null;

    @Autowired
    public void setSenders(List<ReactiveWatchEventNotificationSender> notificationSenders) {
        this.notificationSenders = notificationSenders;
    }

    @Override
    public Mono<Void> consume(TextWatchEvent event) {
        return watchSubscriptionService
                .findAllSubscribersBySubscription(event.getModelId())
                .flatMap(subscribers -> {
                    event.setSubscribers(collectionToSet(subscribers));
                    return eventRepository.save(event);
                })
                .flatMap(e ->
                        Flux.defer(() ->
                                Flux.fromIterable(notificationSenders).map(
                                        reactiveWatchEventNotificationSender -> reactiveWatchEventNotificationSender.send(
                                                event.getModelId(), event.toDto()
                                        )
                                )
                        ).then()
                );
    }

    private Set<String> collectionToSet(Collection<String> subscribers) {
        Set<String> set = new HashSet<>(subscribers);
        return Collections.unmodifiableSet(set);
    }
}
