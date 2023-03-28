package io.extremum.datetime;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ZonedDateTimes {
    public static ZonedDateTime fromEpochMicros(long epochMicros, ZoneId resultZoneId) {
        Instant instant = Instants.fromEpochMicros(epochMicros);
        return ZonedDateTime.ofInstant(instant, resultZoneId);
    }

    public static long toEpochMicros(ZonedDateTime dateTime) {
        return Instants.toEpochMicros(dateTime.toInstant());
    }

    private ZonedDateTimes() {}
}
