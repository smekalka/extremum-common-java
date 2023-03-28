package io.extremum.watch.controller;

import io.extremum.watch.processor.StompHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ReactiveWebSocketHandler implements WebSocketHandler {
    private final StompHandler stompHandler;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return stompHandler.handle(session);
    }
}
