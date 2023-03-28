package io.extremum.common.exceptions;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author rpuch
 */
class ConverterNotFoundExceptionTest {
    @Test
    void exceptionErrorCodeShouldBe500() {
        ConverterNotFoundException e = new ConverterNotFoundException("Cannot find a converter");
        assertThat(e.getCode(), is(500));
    }
}