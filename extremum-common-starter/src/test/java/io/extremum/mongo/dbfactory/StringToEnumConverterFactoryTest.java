package io.extremum.mongo.dbfactory;

import io.extremum.common.annotation.FromStorageString;
import io.extremum.mongo.config.StringToEnumConverterFactory;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.converter.Converter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author rpuch
 */
class StringToEnumConverterFactoryTest {
    private final StringToEnumConverterFactory converterFactory = new StringToEnumConverterFactory();

    @Test
    void convertsWithFromStorageStringMethod() {
        Converter<String, AnnotatedEnum> converter = converterFactory.getConverter(AnnotatedEnum.class);
        assertThat(converter.convert("value"), is(AnnotatedEnum.VALUE));
    }

    @Test
    void convertsUsingNameIfThereIsNoFromStorageStringMethod() {
        Converter<String, NotAnnotatedEnum> converter = converterFactory.getConverter(NotAnnotatedEnum.class);
        assertThat(converter.convert("VALUE"), is(NotAnnotatedEnum.VALUE));
    }
    
    private enum AnnotatedEnum {
        VALUE;

        @FromStorageString
        public static AnnotatedEnum fromStringValue(String value) {
            if ("value".equals(value)) {
                return VALUE;
            }
            throw new IllegalStateException(String.format("Unsupported value: '%s'", value));
        }
    }

    private enum NotAnnotatedEnum {
        VALUE
    }
}