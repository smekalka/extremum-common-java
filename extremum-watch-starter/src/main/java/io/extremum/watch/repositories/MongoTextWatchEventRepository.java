package io.extremum.watch.repositories;

import io.extremum.watch.config.conditional.BlockingWatchConfiguration;
import io.extremum.watch.models.TextWatchEvent;
import org.bson.types.ObjectId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
@ConditionalOnBean(BlockingWatchConfiguration.class)
//TODO add inheritance for WatchEvent and maintain it on repositories
public interface MongoTextWatchEventRepository extends MongoRepository<TextWatchEvent, ObjectId> {
    List<TextWatchEvent> findBySubscribersAndCreatedBetweenOrderByCreatedAscIdAsc(String subscriber,
            ZonedDateTime since, ZonedDateTime until, Pageable pageable);
}
