package io.extremum.datetime;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZonedDateTime;

import static io.extremum.test.hamcrest.SameMomentMatcher.atSameMomentAs;
import static org.hamcrest.MatcherAssert.assertThat;

class InstantsTest {
    @Test
    void fromEpochMicrosShouldWorkCorrectly() {
        ZonedDateTime dateTime = ZonedDateTime.parse("2020-12-31T23:59:59.123Z");
        long microseconds = dateTime.toInstant().toEpochMilli() * 1000 + 456;

        Instant instant = Instants.fromEpochMicros(microseconds);

        assertThat(instant, atSameMomentAs(ZonedDateTime.parse("2020-12-31T23:59:59.123456Z")));
    }

    @Test
    void toEpochMicrosShouldWorkCorrectly() {
        ZonedDateTime dateTime = ZonedDateTime.parse("2020-12-31T23:59:59.123456Z");
        long microseconds = Instants.toEpochMicros(dateTime.toInstant());

        Instant instant = Instant.ofEpochSecond(microseconds / 1_000_000, microseconds % 1_000_000 * 1000);

        assertThat(instant, atSameMomentAs(ZonedDateTime.parse("2020-12-31T23:59:59.123456Z")));
    }
}