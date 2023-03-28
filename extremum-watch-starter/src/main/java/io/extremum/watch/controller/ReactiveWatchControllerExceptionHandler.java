package io.extremum.watch.controller;

import io.extremum.security.ExtremumAccessDeniedException;
import io.extremum.sharedmodels.dto.Response;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

@Component
@Order(-2)
@ConditionalOnBean(ReactiveWatchController.class)
@ConditionalOnProperty(prefix = "custom", value = "watch.reactive", havingValue = "true")
public class ReactiveWatchControllerExceptionHandler extends AbstractErrorWebExceptionHandler {
    public ReactiveWatchControllerExceptionHandler(ErrorAttributes errorAttributes,
                                                   WebProperties webProperties,
                                                   ApplicationContext applicationContext,
                                                   ServerCodecConfigurer serverCodecConfigurer) {
        super(errorAttributes, webProperties.getResources(), applicationContext);
        setMessageWriters(serverCodecConfigurer.getWriters());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(
                RequestPredicates.path("/watch/**"),
                this::renderErrorResponse);
    }

    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Throwable error = getError(request);
        int status;

        if (error instanceof ExtremumAccessDeniedException) {
            status = HttpStatus.FORBIDDEN.value();
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        }

        Response response = Response.builder()
                .withFailStatus(status)
                .withNowTimestamp()
                .build();

        return ServerResponse
                .status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(response));
    }
}
