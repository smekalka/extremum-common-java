package io.extremum.test.mockito;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author rpuch
 */
public class ReturnFirstArg<T> implements Answer<T> {
    public static <T> Answer<T> returnFirstArg() {
        return new ReturnFirstArg<>();
    }

    @Override
    public T answer(InvocationOnMock invocation) {
        return invocation.getArgument(0);
    }
}
