package io.extremum.watch.services;

import io.extremum.watch.models.TextWatchEvent;
import io.extremum.watch.repositories.jpa.JpaTextWatchEventRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.time.ZonedDateTime;
import java.util.List;

@AllArgsConstructor
public class JpaTextWatchEventService implements TextWatchEventService {
    private final JpaTextWatchEventRepository textWatchEventRepository;

    @Override
    public void save(TextWatchEvent event) {
        textWatchEventRepository.save(event);
    }

    @Override
    public List<TextWatchEvent> findBySubscribersAndCreatedBetweenOrderByCreatedAscIdAsc(String subscriber, ZonedDateTime orElse, ZonedDateTime orElse1, Pageable orElse2) {
        return textWatchEventRepository.findBySubscribersAndCreatedBetweenOrderByCreatedAscIdAsc(subscriber, orElse, orElse1, orElse2);
    }
}
