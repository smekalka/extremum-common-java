package io.extremum.watch.controller;

import io.extremum.datetime.DateConstants;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.beans.ConstructorProperties;
import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * @author rpuch
 */
@ToString
public class GetEventsRequest {
    private final ZonedDateTime since;
    private final ZonedDateTime until;
    private final Integer limit;

    @ConstructorProperties({"since", "until", "limit"})
    public GetEventsRequest(
            @DateTimeFormat(pattern = DateConstants.DATETIME_FORMAT_WITH_MICROS) ZonedDateTime since,
            @DateTimeFormat(pattern = DateConstants.DATETIME_FORMAT_WITH_MICROS) ZonedDateTime until,
            Integer limit) {
        this.since = since;
        this.until = until;
        this.limit = limit;
    }

    public Optional<ZonedDateTime> getSince() {
        return Optional.ofNullable(since);
    }

    public Optional<ZonedDateTime> getUntil() {
        return Optional.ofNullable(until);
    }

    public Optional<Integer> getLimit() {
        return Optional.ofNullable(limit);
    }
}
