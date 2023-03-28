package io.extremum.starter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.time.ZonedDateTime;
import java.util.Date;

/**
 * @author rpuch
 */
@WritingConverter
public class ZonedDateTimeToDateConverter implements Converter<ZonedDateTime, Date> {
    @Override
    public Date convert(ZonedDateTime source) {
        if (source == null) {
            return null;
        }
        return Date.from(source.toInstant());
    }
}
