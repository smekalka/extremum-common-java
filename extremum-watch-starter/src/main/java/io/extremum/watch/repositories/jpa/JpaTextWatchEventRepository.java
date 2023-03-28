package io.extremum.watch.repositories.jpa;

import io.extremum.watch.config.conditional.BlockingWatchConfiguration;
import io.extremum.watch.models.TextWatchEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@ConditionalOnBean(BlockingWatchConfiguration.class)
public interface JpaTextWatchEventRepository extends JpaRepository<TextWatchEvent, UUID> {
    List<TextWatchEvent> findBySubscribersAndCreatedBetweenOrderByCreatedAscIdAsc(String subscriber, ZonedDateTime since, ZonedDateTime until, Pageable pageable);
}
