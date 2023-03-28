package io.extremum.test.mockito;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public class ReturnFirstArgInMono<T> implements Answer<Mono<T>> {
    public static <T> Answer<Mono<T>> returnFirstArgInMono() {
        return new ReturnFirstArgInMono<>();
    }

    @Override
    public Mono<T> answer(InvocationOnMock invocation) {
        return Mono.just(invocation.getArgument(0));
    }
}
