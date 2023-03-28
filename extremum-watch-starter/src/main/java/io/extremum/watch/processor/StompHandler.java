package io.extremum.watch.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.mapper.jackson.BasicJsonObjectMapper;
import io.extremum.watch.config.conditional.ReactiveWatchConfiguration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Component
@ConditionalOnBean(ReactiveWatchConfiguration.class)
public class StompHandler {
    @AllArgsConstructor
    @Slf4j
    protected static class Frame {
        static final String STOMP = "STOMP";
        static final String CONNECT = "CONNECT";
        static final String CONNECTED = "CONNECTED";
        static final String DISCONNECT = "DISCONNECT";
        static final String SUBSCRIBE = "SUBSCRIBE";
        static final String UNSUBSCRIBE = "UNSUBSCRIBE";
        static final String RECEIPT = "RECEIPT";
        static final String MESSAGE = "MESSAGE";
        static final String ERROR = "ERROR";

        String frameType;
        Map<String, String> headers;
        String body;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(frameType);
            sb.append('\n');
            for (Map.Entry<String, String> h: headers.entrySet()) {
                sb.append(h.getKey() + ':' + h.getValue());
                sb.append('\n');
            }
            sb.append('\n');
            if (body != null) {
                sb.append(body);
            }
            sb.append('\u0000');

            return sb.toString();
        }

        static Frame parse(String raw) {
            String rawWithoutNullChar;
            if (raw.endsWith("\u0000")) {
                rawWithoutNullChar = raw.substring(0, raw.length() - 1);
            } else {
                rawWithoutNullChar = raw;
            }
            String[] lines = rawWithoutNullChar.split("\\n");

            if (lines.length < 1 || lines[0].isEmpty()) {
                throw new IllegalArgumentException("Missing frame type");
            }

            String frame = lines[0];

            Map<String, String> headers = new HashMap<>();
            int i = 1;
            while (i < lines.length && !lines[i].isEmpty()) {
                int colonPos = lines[i].indexOf(':');
                if (colonPos > 0 ) {
                    headers.put(lines[i].substring(0, colonPos), lines[i].substring(colonPos + 1));
                } else {
                    log.debug("Bad STOMP header: '{}'", lines[i]);
                }

                i++;
            }

            i++;

            String body = null;
            if (i < lines.length) {
                body = Arrays.stream(lines).skip(i).collect(Collectors.joining("\n"));
            }

            return new Frame(frame, headers, body);
        }
    }

    @AllArgsConstructor
    protected static class Subscription {
        String id;
        String destination;
        WebSocketSession session;
    }

    private final ObjectMapper objectMapper = new BasicJsonObjectMapper();

    @Getter
    private final Map<String, Subscription> subscriptions = new ConcurrentHashMap<>();
    private final Map<String, EmitterProcessor<WebSocketMessage>> emitters = new ConcurrentHashMap<>();

    protected void processFrame(Frame incomingFrame, WebSocketSession session, Consumer<Frame> frameConsumer) {
        if (incomingFrame.headers.containsKey("receipt")) {
            Map<String, String> receiptHeaders = new HashMap<>();
            receiptHeaders.put("receipt-id", incomingFrame.headers.get("receipt"));
            Frame receipt = new Frame(Frame.RECEIPT, receiptHeaders, null);
            frameConsumer.accept(receipt);
        }

        Frame response = null;
        Map<String, String> headers = new HashMap<>();

        switch (incomingFrame.frameType) {
            case Frame.STOMP:
            case Frame.CONNECT:
                headers.put("version", "1.1");
                headers.put("heart-beat", "0,0");
                response = new Frame(Frame.CONNECTED, headers, null);
                break;
            case Frame.DISCONNECT:
                break;
            case Frame.SUBSCRIBE:
                String id = incomingFrame.headers.get("id");
                String destination = incomingFrame.headers.get("destination");
                subscriptions.put(destination, new Subscription(id, destination, session));
                break;
            case Frame.UNSUBSCRIBE:
                String unsubscribeId = incomingFrame.headers.get("id");
                Set<String> keys = subscriptions.entrySet()
                        .stream()
                        .filter(e -> e.getValue().id  == unsubscribeId)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toSet());
                keys.forEach(subscriptions::remove);
                break;
            default:
                response = new Frame(Frame.ERROR, headers, "Not implemented");
                break;
        }

        if (response != null) {
            frameConsumer.accept(response);
        }
    }

    public Mono<Void> handle(WebSocketSession session) {
        EmitterProcessor<WebSocketMessage> emitter = EmitterProcessor.create();
        session.receive().subscribe(message -> {
            Frame stompFrame = Frame.parse(message.getPayloadAsText());

            processFrame(
                    stompFrame,
                    session,
                    outgoingFrame -> emitter.onNext(session.textMessage(outgoingFrame.toString())));
        });
        emitters.put(session.getId(), emitter);
        return session.send(emitter.publish().autoConnect());
    }

    protected Frame createMessageFrame(Subscription subscription, Object payload) throws JsonProcessingException {
        String body = objectMapper.writeValueAsString(payload);
        Map<String, String> headers = new HashMap<>();

        headers.put("destination", subscription.destination);
        headers.put("content-type", "application/json");
        headers.put("subscription", subscription.id);
        headers.put("message-id", UUID.randomUUID().toString());
        headers.put("content-length", String.valueOf(body.getBytes().length));

        return new Frame(Frame.MESSAGE, headers, body);
    }

    public Mono<Void> send(String user, String destination, Object payload) {
        String subscriptionKey = "/user/" + user + destination;
        Subscription subscription = subscriptions.get(subscriptionKey);
        if (subscription != null) {
            EmitterProcessor<WebSocketMessage> emitter = emitters.get(subscription.session.getId());
            if (emitter != null) {
                try {
                    Frame frame = createMessageFrame(subscription, payload);
                    emitter.onNext(subscription.session.textMessage(frame.toString()));
                } catch (JsonProcessingException e) {
                    log.error("JSON serialization error", e);
                }
            }
        }
        return Mono.empty();
    }
}
