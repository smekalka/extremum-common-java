package io.extremum.watch.processor;

import io.extremum.watch.models.TextWatchEvent;
import reactor.core.publisher.Mono;

public interface ReactiveWatchEventConsumer {
    Mono<Void> consume(TextWatchEvent event);
}
