package io.extremum.watch.services;

import io.extremum.common.spring.data.OffsetBasedPageRequest;
import io.extremum.mongo.MongoConstants;
import io.extremum.watch.config.conditional.ReactiveWatchConfiguration;
import io.extremum.watch.models.TextWatchEvent;
import io.extremum.watch.repositories.ReactiveTextWatchEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.ZonedDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@ConditionalOnBean(ReactiveWatchConfiguration.class)
public class PersistentReactiveWatchEventService implements ReactiveWatchEventService {
    private final ReactiveTextWatchEventRepository eventRepository;

    @Override
    public Flux<TextWatchEvent> findEvents(String subscriber, Optional<ZonedDateTime> since,
                                           Optional<ZonedDateTime> until, Optional<Integer> limit) {
        return eventRepository.findBySubscribersAndCreatedBetweenOrderByCreatedAscIdAsc(subscriber,
                since.orElse(MongoConstants.DISTANT_PAST),
                until.orElse(MongoConstants.DISTANT_FUTURE),
                limit.map(OffsetBasedPageRequest::limit).orElse(Pageable.unpaged())
        );
    }
}