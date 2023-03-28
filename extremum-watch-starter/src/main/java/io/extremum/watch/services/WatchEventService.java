package io.extremum.watch.services;

import io.extremum.watch.models.TextWatchEvent;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface WatchEventService {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    List<TextWatchEvent> findEvents(String principal, Optional<ZonedDateTime> since, Optional<ZonedDateTime> until,
            Optional<Integer> limit);
}
