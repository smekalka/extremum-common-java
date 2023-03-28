package io.extremum.datetime;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static io.extremum.test.hamcrest.SameMomentMatcher.atSameMomentAs;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

class PreciseDateTimesTest {
    private ZonedDateTime inprecise = ZonedDateTime.parse("2020-12-31T23:59:59.123Z");

    @Test
    void constructsDateTimeWithMicrosecondPrecisionWhenBothDateTimeAndMicrosAreGiven() {
        ZonedDateTime precise = ZonedDateTime.parse("2020-12-31T23:59:59.123456Z");

        ZonedDateTime reconstruction = PreciseDateTimes.preciseZonedDateTime(
                inprecise, ZonedDateTimes.toEpochMicros(precise));

        assertThat(reconstruction, atSameMomentAs(precise));
    }

    @Test
    void returnsNullWhenInpreciseDateTimeIsNull() {
        assertThat(PreciseDateTimes.preciseZonedDateTime(null, null), is(nullValue()));
    }

    @Test
    void returnsInpreciseDateTimeIfMicrosecondsAreNotGiven() {
        assertThat(PreciseDateTimes.preciseZonedDateTime(inprecise, null), is(inprecise));
    }
}