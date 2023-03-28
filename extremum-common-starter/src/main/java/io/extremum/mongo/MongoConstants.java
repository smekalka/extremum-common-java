package io.extremum.mongo;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * @author rpuch
 */
public class MongoConstants {
    public static final ZonedDateTime DISTANT_PAST = ZonedDateTime.of(
            LocalDateTime.of(0, Month.JANUARY, 1, 0, 0, 0),
            ZoneId.of("UTC")
    );
    public static final ZonedDateTime DISTANT_FUTURE = ZonedDateTime.of(
            LocalDateTime.of(9999, Month.DECEMBER, 31, 23, 59, 59),
            ZoneId.of("UTC")
    );
}
