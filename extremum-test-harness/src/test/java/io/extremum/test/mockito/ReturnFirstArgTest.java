package io.extremum.test.mockito;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReturnFirstArgTest {
    @Mock
    private InvocationOnMock invocation;

    @Test
    void returnsFirstArg() throws Throwable {
        when(invocation.getArgument(0)).thenReturn("arg");

        Object answer = ReturnFirstArg.returnFirstArg().answer(invocation);

        assertThat(answer, is("arg"));
    }
}