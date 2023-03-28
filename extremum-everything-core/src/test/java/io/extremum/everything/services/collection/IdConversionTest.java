package io.extremum.everything.services.collection;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author rpuch
 */
class IdConversionTest {
    private static final ObjectId OBJECT_ID = new ObjectId();

    private final IdConversion idConversion = new IdConversion();

    @Test
    void testConversion() {
        assertThat(idConversion.convert("abc", String.class), is("abc"));
        assertThat(idConversion.convert(OBJECT_ID.toString(), ObjectId.class), is(OBJECT_ID));
        assertThat(idConversion.convert(OBJECT_ID, String.class), is(OBJECT_ID.toString()));
        assertThat(idConversion.convert(OBJECT_ID, ObjectId.class), is(OBJECT_ID));
    }

    @Test
    void whenSourceIsNotSupported_thenAnExceptionShouldBeThrown() {
        try {
            idConversion.convert(1.23, String.class);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Cannot convert from 'class java.lang.Double' to 'class java.lang.String'"));
        }
    }

    @Test
    void whenTargetIsNotSupported_thenAnExceptionShouldBeThrown() {
        try {
            idConversion.convert("123", Double.class);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(),
                    is("Cannot convert from 'class java.lang.String' to 'class java.lang.Double'"));
        }
    }
}