package io.extremum.datetime;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static io.extremum.test.hamcrest.SameMomentMatcher.atSameMomentAs;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ZonedDateTimesTest {
    @Test
    void fromEpochMicrosShouldYieldCorrectDateTime() {
        ZonedDateTime originalDateTime = ZonedDateTime.parse("2020-12-31T23:59:59.123Z");
        long microseconds = originalDateTime.toInstant().toEpochMilli() * 1000 + 456;

        ZonedDateTime result = ZonedDateTimes.fromEpochMicros(microseconds, ZoneId.systemDefault());

        assertThat(result, atSameMomentAs(ZonedDateTime.parse("2020-12-31T23:59:59.123456Z")));
    }

    @Test
    void fromEpochMicrosShouldYieldDateTimeWithTheRequestedZoneId() {
        ZoneOffset zone = ZoneOffset.of("+07:00");

        ZonedDateTime result = ZonedDateTimes.fromEpochMicros(0L, zone);

        assertThat(result.getZone(), is(zone));
    }

    @Test
    void toEpochMicrosShouldWorkCorrectly() {
        ZonedDateTime dateTime = ZonedDateTime.parse("2020-12-31T23:59:59.123456Z");
        long microseconds = ZonedDateTimes.toEpochMicros(dateTime);

        Instant instant = Instant.ofEpochSecond(microseconds / 1_000_000, microseconds % 1_000_000 * 1000);

        assertThat(instant, atSameMomentAs(ZonedDateTime.parse("2020-12-31T23:59:59.123456Z")));
    }
}