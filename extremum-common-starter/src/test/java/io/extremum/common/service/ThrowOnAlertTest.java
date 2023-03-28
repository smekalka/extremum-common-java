package io.extremum.common.service;

import io.extremum.common.exceptions.CommonException;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author rpuch
 */
public class ThrowOnAlertTest {
    private final Problems problems = new ThrowOnAlert();
    private final CommonException exception = new CommonException("test", 200);

    @Test
    public void whenAnExceptionIsConsumed_thenItShouldBeThrown() {
        try {
            problems.accept(exception);
            fail("An exception should be thrown");
        } catch (CommonException e) {
            assertThat(e, is(sameInstance(exception)));
        }
    }
}