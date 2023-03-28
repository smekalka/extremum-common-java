package io.extremum.datetime;

import java.time.Instant;

import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Instants {

    public static Instant fromEpochMicros(long epochMicros) {
        long epochSeconds = MICROSECONDS.toSeconds(epochMicros);
        long nanoAdjustment = MICROSECONDS.toNanos(epochMicros % 1_000_000);
        return Instant.ofEpochSecond(epochSeconds, nanoAdjustment);
    }

    public static long toEpochMicros(Instant instant) {
        return SECONDS.toMicros(instant.getEpochSecond()) + NANOSECONDS.toMicros(instant.getNano());
    }

    private Instants() {}
}
