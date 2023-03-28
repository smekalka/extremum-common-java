package io.extremum.common.logging;

import io.extremum.sharedmodels.logging.LoggingConstants;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import reactor.util.context.Context;

import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ReactiveLoggingTest {
    @Test
    void whenLoggingContextually_thenMDCShouldHaveTheRequestIdDuringLoggingActionExecution() {
        AtomicReference<String> reference = new AtomicReference<>();

        ReactiveLogging.logContextually(() -> reference.set(MDC.get(LoggingConstants.REQUEST_ID_ATTRIBUTE_NAME)))
                .subscriberContext(Context.of(LoggingConstants.REQUEST_ID_ATTRIBUTE_NAME, "uuid"))
                .block();

        assertThat(reference.get(), is("uuid"));
    }
}