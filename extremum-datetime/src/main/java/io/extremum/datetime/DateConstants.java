package io.extremum.datetime;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class DateConstants {
    /**
     * The format that is used to parse/format 'standard' ISO-8601 date-times in extended format
     * (i.e. having '-' and ':' separators). The difference from {@link DateTimeFormatter#ISO_OFFSET_DATE_TIME}
     * (used in {@link OffsetDateTime#parse(CharSequence, DateTimeFormatter)} and {@link OffsetDateTime#toString()})
     * is that parse()/toString() allow arbitrary number of fractional digits of second (actually, 0 to 9), and
     * our format strictly requires exactly 6 digits.
     *
     * This format accepts (and generates) date-time strings with zone offset of two formats: either literal 'Z',
     * or hours:minutes like '+03:00' or '-09:00'. It does <b>NOT</b> support (nor produce) offsets in basic format,
     * like '+0300' (without a colon).
     *
     * In Internet, you can find a lot of examples of 'ISO-8601 patterns' like "yyyy-MM-dd'T'HH:mm:ssZ"
     * (with 'Z' on the end). Please note that this produces <b>incorrect</b> date-time strings because Z pattern
     * character produces zone offset in basic format (like '+0300', without a colon), and, according to
     * paragraph 4.3.3d of the ISO-8601 standard paper, it is not allowed to mix a date-time in extended format
     * (which we expect) with zone offset in basic format.
     * Another problem with that 'Z-ending pattern' is that it does not support zone offsets represented with
     * literal 'Z'. For example, it will fail to parse date-time like '2020-01-02T23:59:59Z' which is perfectly
     * valid according to ISO-8601 standard.
     */
    public static final String DATETIME_FORMAT_WITH_MICROS = "uuuu-MM-dd'T'HH:mm:ss.SSSSSSXXX";
    public static final DateTimeFormatter RFC_ZONED_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
            "EEE, dd MMM yyyy HH:mm:ss zzz");
    public static final DateTimeFormatter ISO_8601_ZONED_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
            DATETIME_FORMAT_WITH_MICROS);

    private DateConstants() {
        throw new UnsupportedOperationException();
    }
}
