package io.extremum.watch.repositories;

import io.extremum.watch.config.conditional.ReactiveWatchConfiguration;
import io.extremum.watch.models.TextWatchEvent;
import org.bson.types.ObjectId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.ZonedDateTime;

@Repository
@ConditionalOnBean(ReactiveWatchConfiguration.class)
//TODO add inheritance for WatchEvent and maintain it on repositories
public interface ReactiveTextWatchEventRepository extends ReactiveMongoRepository<TextWatchEvent, ObjectId> {
    Flux<TextWatchEvent> findBySubscribersAndCreatedBetweenOrderByCreatedAscIdAsc(String subscriber,
                                                                                  ZonedDateTime since, ZonedDateTime until, Pageable pageable);
}

