package io.extremum.mongo.dbfactory;

import io.extremum.common.annotation.ToStorageString;
import io.extremum.mongo.config.EnumToStringConverter;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author rpuch
 */
class EnumToStringConverterTest {
    private final EnumToStringConverter converter = new EnumToStringConverter();

    @Test
    void convertsWithToStringMethod() {
        assertThat(converter.convert(AnnotatedEnum.VALUE), is("value"));
    }
    
    private enum AnnotatedEnum {
        VALUE;

        @ToStorageString
        public String toStringValue() {
            return "value";
        }
    }
}