package io.extremum.watch.services;

import io.extremum.common.spring.data.OffsetBasedPageRequest;
import io.extremum.mongo.MongoConstants;
import io.extremum.watch.config.conditional.BlockingWatchConfiguration;
import io.extremum.watch.models.TextWatchEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@ConditionalOnBean(BlockingWatchConfiguration.class)
public class PersistentWatchEventService implements WatchEventService {
    private final TextWatchEventService service;

    @Override
    public List<TextWatchEvent> findEvents(String subscriber, Optional<ZonedDateTime> since,
                                           Optional<ZonedDateTime> until, Optional<Integer> limit) {
        return service.findBySubscribersAndCreatedBetweenOrderByCreatedAscIdAsc(subscriber,
                since.orElse(MongoConstants.DISTANT_PAST),
                until.orElse(MongoConstants.DISTANT_FUTURE),
                limit.map(OffsetBasedPageRequest::limit).orElse(Pageable.unpaged())
        );
    }
}
