package io.extremum.watch.processor;

import io.extremum.watch.dto.TextWatchEventNotificationDto;
import io.extremum.watch.dto.WebSocketNotificationDto;
import io.extremum.watch.services.ReactiveWatchSubscriptionService;
import io.extremum.watch.services.WatchSubscriptionService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ReactiveWebSocketWatchEventNotificationSender implements ReactiveWatchEventNotificationSender {
    private final StompHandler stompHandler;
    private final ReactiveWatchSubscriptionService subscriptionService;

    protected Mono<String> getJsonPatch(String modelId, String subscriberId, TextWatchEventNotificationDto notificationDto) {
        return subscriptionService.isFreshSubscription(modelId, subscriberId)
            .map(isFresh -> {
                if (isFresh && notificationDto.getFullReplacePatch() != null) {
                    return notificationDto.getFullReplacePatch();
                } else {
                    return notificationDto.getJsonPatch();
                }
            });
    }

    @Override
    public Mono<Void> send(String modelId, TextWatchEventNotificationDto notificationDto) {
        return Flux.fromIterable(notificationDto.getSubscribers())
                .flatMap(subscriberId ->
                    getJsonPatch(modelId, subscriberId, notificationDto)
                            .map(jsonPatch -> stompHandler.send(subscriberId, "/watch", new WebSocketNotificationDto(jsonPatch)))
                )
                .then();
    }
}
