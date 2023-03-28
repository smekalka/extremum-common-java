package io.extremum.datetime;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Format used to convert between date-time objects and their string representation according to our API format.
 * The format is ISO-8601 date-time with offset which always have exactly 6 fractional digits of a second.
 * See {@link DateConstants#DATETIME_FORMAT_WITH_MICROS}.
 *
 * @author rpuch
 * @see DateConstants#DATETIME_FORMAT_WITH_MICROS
 */
public class ApiDateTimeFormat {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DateConstants.DATETIME_FORMAT_WITH_MICROS);

    public String format(ZonedDateTime dateTime) {
        return dateTime.format(formatter);
    }

    public ZonedDateTime parse(String string) {
        return ZonedDateTime.parse(string, formatter);
    }
}
