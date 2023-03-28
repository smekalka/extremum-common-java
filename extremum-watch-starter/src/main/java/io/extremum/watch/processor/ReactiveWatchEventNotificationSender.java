package io.extremum.watch.processor;

import io.extremum.watch.dto.TextWatchEventNotificationDto;
import reactor.core.publisher.Mono;

public interface ReactiveWatchEventNotificationSender {
    Mono<Void> send(String modelId, TextWatchEventNotificationDto notificationDto);
}
