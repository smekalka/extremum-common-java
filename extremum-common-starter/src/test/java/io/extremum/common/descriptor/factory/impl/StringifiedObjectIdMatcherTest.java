package io.extremum.common.descriptor.factory.impl;

import org.bson.types.ObjectId;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author rpuch
 */
class StringifiedObjectIdMatcherTest {
    private final Matcher<String> matcher = StringifiedObjectIdMatcher.objectId();

    @Test
    void givenAStringIsAnObjectIdRepresentation_whenMatchingItWithObjectIdMatcher_thenItShouldMatch() {
        String objectIdRepresentation = new ObjectId().toString();
        assertTrue(matcher.matches(objectIdRepresentation));
    }

    @Test
    void givenAStringIsNull_whenMatchingItWithObjectIdMatcher_thenItShouldNotMatch() {
        assertFalse(matcher.matches(null));
    }

    @Test
    void givenAStringIsNotAnObjectIdRepresentation_whenMatchingItWithObjectIdMatcher_thenItShouldNotMatch() {
        assertFalse(matcher.matches("abc"));
    }

    @Test
    void givenAStringIsNotAnObjectIdRepresentation_whenDescribingWithObjectIdMatcher_thenItShouldProduceAnExpectedDescription() {
        try {
            assertThat("abc", matcher);
            fail("An AssertionError is expected");
        } catch (AssertionError e) {
            assertThat(e.getMessage(), is("\nExpected: String representation of ObjectId\n     but: was \"abc\""));
        }
    }
}