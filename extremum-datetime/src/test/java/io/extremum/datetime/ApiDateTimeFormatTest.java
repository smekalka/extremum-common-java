package io.extremum.datetime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

import static io.extremum.test.hamcrest.SameMomentMatcher.atSameMomentAs;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApiDateTimeFormatTest {
    private static final ZonedDateTime MOMENT_WITH_MICROS = expectedMoment(123_456_000);
    private static final ZonedDateTime MOMENT_WITH_MILLIS = expectedMoment(123_000_000);
    private static final ZonedDateTime MOMENT_WITH_SECONDS = expectedMoment(0);
    private static final ZoneId PLUS_3_HOURS = ZoneId.of("+03:00");

    private final ApiDateTimeFormat format = new ApiDateTimeFormat();

    private static ZonedDateTime expectedMoment(int nanoOfSecond) {
        return ZonedDateTime.of(2020, 1, 1,
                13, 59, 59,
                nanoOfSecond,
                ZoneId.of("UTC"));
    }

    @Test
    void failsToParseDateTimeWithOffsetInBasicFormatAndMicroseconds() {
        assertParsingFails(() -> format.parse("2020-01-01T13:59:59.123456+0000"));
    }

    @Test
    void failsToParseDateTimeWithOffsetInBasicFormatAndMilliseconds() {
        assertParsingFails(() -> format.parse("2020-01-01T13:59:59.123+0000"));
    }

    private void assertParsingFails(Executable parseAction) {
        assertThrows(DateTimeParseException.class, parseAction);
    }

    @Test
    void failsToParseDateTimeWithOffsetInBasicFormatAndJustSeconds() {
        assertParsingFails(() -> format.parse("2020-01-01T13:59:59+0000"));
    }

    @Test
    void parsesDateTimeWithOffsetInExtendedFormatAndMicroseconds() {
        ZonedDateTime dateTime = format.parse("2020-01-01T13:59:59.123456+00:00");

        assertThat(dateTime, atSameMomentAs(MOMENT_WITH_MICROS));
    }

    @Test
    void failsToParseDateTimeWithOffsetInExtendedFormatAndMilliseconds() {
        assertParsingFails(() -> format.parse("2020-01-01T13:59:59.123+00:00"));
    }

    @Test
    void failsToParseDateTimeWithOffsetInExtendedFormatAndJustSeconds() {
        assertParsingFails(() -> format.parse("2020-01-01T13:59:59+00:00"));
    }

    @Test
    void parsesDateTimeWithOffsetAsZAndMicroseconds() {
        ZonedDateTime dateTime = format.parse("2020-01-01T13:59:59.123456Z");

        assertThat(dateTime, atSameMomentAs(MOMENT_WITH_MICROS));
    }

    @Test
    void failsToParseDateTimeWithOffsetAsZAndMilliseconds() {
        assertParsingFails(() -> format.parse("2020-01-01T13:59:59.123Z"));
    }

    @Test
    void failsToParseDateTimeWithOffsetAsZAndJustSeconds() {
        assertParsingFails(() -> format.parse("2020-01-01T13:59:59Z"));
    }

    @Test
    void failsParsingWhenGarbageIsGiven() {
        assertThrows(DateTimeParseException.class, () -> format.parse("invalid-date-time"));
    }

    @Test
    void formatsDateTimeWithMicrosecondsInExtendedISO8601FormatWith6FractionalDigits() {
        String formatted = format.format(MOMENT_WITH_MICROS);

        assertThat(formatted, equalTo("2020-01-01T13:59:59.123456Z"));
    }

    @Test
    void formatsDateTimeWithMillisecondsInExtendedISO8601Format6FractionalDigits() {
        String formatted = format.format(MOMENT_WITH_MILLIS);

        assertThat(formatted, equalTo("2020-01-01T13:59:59.123000Z"));
    }

    @Test
    void formatsDateTimeWithJustSecondsInExtendedISO8601Format6FractionalDigits() {
        String formatted = format.format(MOMENT_WITH_SECONDS);

        assertThat(formatted, equalTo("2020-01-01T13:59:59.000000Z"));
    }

    @Test
    void formatsDateTimeWithNonZeroOffsetInExtendedISO8601Format6FractionalDigits() {
        String formatted = format.format(MOMENT_WITH_MICROS.withZoneSameLocal(PLUS_3_HOURS));

        assertThat(formatted, equalTo("2020-01-01T13:59:59.123456+03:00"));
    }
}