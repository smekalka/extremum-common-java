package io.extremum.common.logging;

import io.extremum.sharedmodels.logging.LoggingConstants;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.UUID;

public class RequestIdProvisionForLoggingFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return Mono.defer(() -> {
            String requestId = randomRequestId();
            exchange.getAttributes().put(LoggingConstants.REQUEST_ID_ATTRIBUTE_NAME, requestId);
            return chain.filter(exchange)
                    .subscriberContext(loggingContext(requestId));
        });
    }

    private Context loggingContext(String requestId) {
        return Context.of(LoggingConstants.REQUEST_ID_ATTRIBUTE_NAME, requestId);
    }

    private String randomRequestId() {
        return UUID.randomUUID().toString();
    }
}
