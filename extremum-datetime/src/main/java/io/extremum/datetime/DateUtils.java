package io.extremum.datetime;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;

import static io.extremum.datetime.DateConstants.DATETIME_FORMAT_WITH_MICROS;
import static io.extremum.datetime.DateConstants.ISO_8601_ZONED_DATE_TIME_FORMATTER;
import static io.extremum.datetime.DateConstants.RFC_ZONED_DATE_TIME_FORMATTER;

/**
 * @author iPolyakov on 03.02.15.
 */
@Slf4j
public final class DateUtils {

    public static SimpleDateFormat dateFormat() {
        return new SimpleDateFormat(DATETIME_FORMAT_WITH_MICROS, Locale.US);
    }

    public static String convert(Date from) {
        return from != null ? dateFormat().format(from) : null;
    }

    public static String convert(ZonedDateTime from) {
        return from.format(DateTimeFormatter.ofPattern(DATETIME_FORMAT_WITH_MICROS));
    }

    public static Date convert(String from) {
        if (from != null) {
            try {
                return dateFormat().parse(from);
            } catch (ParseException e) {
                log.debug("cannot convert Date", e);
            }
        }
        return null;
    }

    /**
     * Parsing string in format "EEE, dd MMM yyyy HH:mm:ss zzz"
     */
    public static ZonedDateTime parseZonedDateTime(String date) {
        return parseZonedDateTime(date, RFC_ZONED_DATE_TIME_FORMATTER);
    }

    /**
     * Parsing string in format "uuuu-MM-dd'T'HH:mm:ss.SSSSSSXXX"
     */
    public static ZonedDateTime parseZonedDateTimeFromISO_8601(String date) {
        return parseZonedDateTime(date, ISO_8601_ZONED_DATE_TIME_FORMATTER);
    }

    public static String formatZonedDateTimeISO_8601(ZonedDateTime date) {
        return date.format(ISO_8601_ZONED_DATE_TIME_FORMATTER);
    }

    public static ZonedDateTime parseZonedDateTime(String date, DateTimeFormatter formatter) {
        if (date != null) {
            try {
                return ZonedDateTime.parse(date, formatter);
            } catch (DateTimeParseException e) {
                log.error("Cannot parse ZonedDateTime", e);
            }
        }
        return null;
    }

    public static ZonedDateTime now() {
        return ZonedDateTime.now();
    }

    public static ZonedDateTime fromInstant(Instant instant) {
        return fromInstant(instant, ZoneOffset.UTC);
    }

    private static ZonedDateTime fromInstant(Instant instant, ZoneOffset zoneOffset) {
        return ZonedDateTime.ofInstant(instant, zoneOffset);
    }

    private DateUtils() {
    }
}
