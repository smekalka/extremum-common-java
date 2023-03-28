package io.extremum.common.codec;

import io.extremum.common.exceptions.ExceptionResponse;
import io.extremum.common.exceptions.handler.annotation.ExtremumExceptionResolver;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.Encoder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.http.codec.ServerSentEventHttpMessageWriter;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
public class ServerSentEventWithErrorMessagesHttpMessageWriter extends ServerSentEventHttpMessageWriter implements HttpMessageWriter<Object> {

    private final ExtremumExceptionResolver extremumExceptionHandler;
    private final ServerSentEventHttpMessageWriter writer;

    public ServerSentEventWithErrorMessagesHttpMessageWriter(ExtremumExceptionResolver extremumExceptionHandler,
            Encoder<?> encoder) {
        this.extremumExceptionHandler = extremumExceptionHandler;
        this.writer = new ServerSentEventHttpMessageWriter(encoder);
    }

    @Override
    public List<MediaType> getWritableMediaTypes() {
        return writer.getWritableMediaTypes();
    }

    @Override
    public boolean canWrite(ResolvableType elementType, MediaType mediaType) {
        return writer.canWrite(elementType, mediaType);
    }

    @Override
    public Mono<Void> write(Publisher<?> input, ResolvableType elementType, MediaType mediaType,
            ReactiveHttpOutputMessage message, Map<String, Object> hints) {
        if (MediaType.TEXT_EVENT_STREAM.equalsTypeAndSubtype(mediaType)) {
            input = Flux.<Object>from(input).onErrorResume(e -> Mono.just(handleWithExtremumExceptionHandler(message, e)));
        }
        return writer.write(input, elementType, mediaType, message, hints);
    }

    private ServerSentEvent<?> handleWithExtremumExceptionHandler(ReactiveHttpOutputMessage message, Throwable e) {
        ExceptionResponse exceptionResponse = extremumExceptionHandler.handleException(e);
        tryToSetStatusCode(message, exceptionResponse.getHttpStatus());
        return ServerSentEvent.builder()
                .event("error")
                .data(exceptionResponse.getData())
                .build();
    }

    private void tryToSetStatusCode(ReactiveHttpOutputMessage message, HttpStatus httpStatus) {
        if (message instanceof ServerHttpResponse) {
            ((ServerHttpResponse) message).setStatusCode(httpStatus);
        }
    }

}
