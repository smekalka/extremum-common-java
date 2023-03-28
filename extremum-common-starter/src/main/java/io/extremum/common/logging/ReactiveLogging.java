package io.extremum.common.logging;

import io.extremum.sharedmodels.logging.LoggingConstants;
import org.slf4j.MDC;
import reactor.core.publisher.Mono;

public class ReactiveLogging {
    public static Mono<?> logContextually(Runnable loggingAction) {
        return Mono.subscriberContext()
                .map(context -> context.getOrDefault(LoggingConstants.REQUEST_ID_ATTRIBUTE_NAME, "<none>"))
                .doOnNext(requestId -> logWithRequestId(loggingAction, requestId));
    }

    private static void logWithRequestId(Runnable loggingAction, String requestId) {
        try (AutoCloseable ignored = MDC.putCloseable(LoggingConstants.REQUEST_ID_ATTRIBUTE_NAME, requestId)) {
            loggingAction.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
