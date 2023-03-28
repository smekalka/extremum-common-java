package io.extremum.watch.services;

import io.extremum.watch.models.TextWatchEvent;
import reactor.core.publisher.Flux;

import java.time.ZonedDateTime;
import java.util.Optional;

public interface ReactiveWatchEventService {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    Flux<TextWatchEvent> findEvents(String principal, Optional<ZonedDateTime> since, Optional<ZonedDateTime> until,
                                    Optional<Integer> limit);
}
