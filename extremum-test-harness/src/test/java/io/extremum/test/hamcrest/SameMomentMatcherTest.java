package io.extremum.test.hamcrest;

import org.hamcrest.StringDescription;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SameMomentMatcherTest {
    @Test
    void matchesDateTimesPointingAtSameMomentButWhichAreNotEqualAccordingToEqualsMethod() {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime atUtc = now.withZoneSameInstant(ZoneId.of("UTC"));
        ZonedDateTime atPlus3 = now.withZoneSameInstant(ZoneId.of("+03:00"));

        assertNotEquals(atUtc, atPlus3);

        SameMomentMatcher<Temporal> matcher = SameMomentMatcher.atSameMomentAs(atUtc);

        assertTrue(matcher.matches(atPlus3));
    }

    @Test
    void buildsCorrectMismatchDescriptionContainingBothExpectedAndActualValues() {
        ZonedDateTime expected = ZonedDateTime.parse("2020-12-31T23:59:59.123456Z");
        ZonedDateTime actual = ZonedDateTime.parse("2020-12-31T23:59:59.123999Z");

        SameMomentMatcher<Temporal> matcher = SameMomentMatcher.atSameMomentAs(expected);

        StringDescription description = new StringDescription();
        matcher.describeMismatch(actual, description);

        assertEquals(
                "<2020-12-31T23:59:59.123999Z> not at the same moment of time as <2020-12-31T23:59:59.123456Z>",
                description.toString());
    }
}