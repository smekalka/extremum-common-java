package io.extremum.starter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * @author rpuch
 */
@ReadingConverter
public class DateToZonedDateTimeConverter implements Converter<Date, ZonedDateTime> {
    @Override
    public ZonedDateTime convert(Date source) {
        if (source == null) {
            return null;
        }
        return ZonedDateTime.ofInstant(source.toInstant(), ZoneId.of("UTC"));
    }
}
