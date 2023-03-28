package io.extremum.watch.services;

import io.extremum.watch.models.TextWatchEvent;
import org.springframework.data.domain.Pageable;

import java.time.ZonedDateTime;
import java.util.List;

public interface TextWatchEventService {

    void save(TextWatchEvent event);

    List<TextWatchEvent> findBySubscribersAndCreatedBetweenOrderByCreatedAscIdAsc(String subscriber, ZonedDateTime orElse, ZonedDateTime orElse1, Pageable orElse2);
}
