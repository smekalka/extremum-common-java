package io.extremum.test.mockito;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReturnFirstArgInMonoTest {
    @Mock
    private InvocationOnMock invocation;

    @Test
    void returnsFirstArgInMono() throws Throwable {
        when(invocation.getArgument(0)).thenReturn("arg");

        Mono<String> answer = ReturnFirstArgInMono.<String>returnFirstArgInMono().answer(invocation);

        StepVerifier.create(answer)
                .expectNext("arg")
                .verifyComplete();
    }
}