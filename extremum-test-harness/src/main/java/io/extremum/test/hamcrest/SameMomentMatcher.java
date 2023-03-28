package io.extremum.test.hamcrest;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.time.Instant;
import java.time.temporal.Temporal;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SameMomentMatcher<T extends Temporal> extends TypeSafeDiagnosingMatcher<T> {
    private final Temporal sample;

    public static <T extends Temporal> SameMomentMatcher<T> atSameMomentAs(Temporal sample) {
        return new SameMomentMatcher<>(sample);
    }

    @Override
    protected boolean matchesSafely(T temporal, Description description) {
        Instant actualInstant = Instant.from(temporal);
        Instant expectedInstant = Instant.from(sample);

        if (actualInstant.equals(expectedInstant)) {
            return true;
        }

        description.appendValue(temporal).appendText(" not at the same moment of time as ").appendValue(sample);
        return false;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("at the same moment of time as ").appendValue(sample);
    }
}
