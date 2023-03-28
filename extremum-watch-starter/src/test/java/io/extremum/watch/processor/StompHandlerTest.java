package io.extremum.watch.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith({MockitoExtension.class})
class StompHandlerTest {
    static final String WEB_SESSION_ID = "12345";
    static final String SUBSCRIPTION_DESTINATION = "/user/Alex/watch";

    StompHandler stompHandler = new StompHandler();

    @Mock
    WebSocketSession webSocketSession;

    @Mock
    Consumer<StompHandler.Frame> frameConsumer;

    @Captor
    ArgumentCaptor<StompHandler.Frame> frameCaptor;

    @Test
    void givenAFrame_whenSerialized_thenACorrectStringIsProduced() {
        String frameString =
                "CONNECTED\n" +
                "heart-beat:0,0\n" +
                "version:1.1\n" +
                "\n\u0000";

        Map<String, String> headers = new TreeMap<>();
        headers.put("version", "1.1");
        headers.put("heart-beat", "0,0");
        StompHandler.Frame frame = new StompHandler.Frame(StompHandler.Frame.CONNECTED, headers, null);
        assertThat(frame.toString(), is(equalTo(frameString)));
    }

    @Test
    void givenAFrameWithBody_whenSerialized_thenACorrectStringIsProduced() {
        String frameString =
                "CONNECTED\n" +
                        "heart-beat:0,0\n" +
                        "version:1.1\n" +
                        "\n" +
                        "Hello!" +
                        "\u0000";

        Map<String, String> headers = new TreeMap<>();
        headers.put("version", "1.1");
        headers.put("heart-beat", "0,0");
        StompHandler.Frame frame = new StompHandler.Frame(StompHandler.Frame.CONNECTED, headers, "Hello!");
        assertThat(frame.toString(), is(equalTo(frameString)));
    }

    @Test
    void givenAValidFrameString_whenParsed_thenACorrectFrameIsProduced() {
        String frameString =
                "CONNECT\n" +
                "accept-version:1.0,1.1,1.2\n" +
                "heart-beat:10000,10000\n" +
                "\n\u0000";

        StompHandler.Frame frame = StompHandler.Frame.parse(frameString);
        assertThat(frame.frameType, is(StompHandler.Frame.CONNECT));
        assertThat(frame.headers, hasEntry("accept-version", "1.0,1.1,1.2"));
        assertThat(frame.headers, hasEntry("heart-beat", "10000,10000"));
        assertThat(frame.body, is(nullValue()));
    }

    @Test
    void givenAValidFrameWithBodyString_whenParsed_thenACorrectFrameIsProduced() {
        String frameString =
                "CONNECT\n" +
                        "accept-version:1.0,1.1,1.2\n" +
                        "heart-beat:10000,10000\n" +
                        "\n" +
                        "Hello!\n" +
                        "\u0000";

        StompHandler.Frame frame = StompHandler.Frame.parse(frameString);
        assertThat(frame.frameType, is(StompHandler.Frame.CONNECT));
        assertThat(frame.headers, hasEntry("accept-version", "1.0,1.1,1.2"));
        assertThat(frame.headers, hasEntry("heart-beat", "10000,10000"));
        assertThat(frame.body, is("Hello!"));
    }

    @Test
    void givenAnInvalidFrameWithBodyString_whenParsed_thenExceptionIsThrown() {
        assertThrows(IllegalArgumentException.class, () -> StompHandler.Frame.parse(""));
    }

    @Test
    void givenConnectFrame_whenProcessed_connectedFrameIsProduced() {
        StompHandler.Frame frame = new StompHandler.Frame(StompHandler.Frame.CONNECT, new HashMap<>(), null);
        stompHandler.processFrame(frame, webSocketSession, frameConsumer);

        verify(frameConsumer).accept(frameCaptor.capture());
        StompHandler.Frame response = frameCaptor.getValue();
        assertThat(response.frameType, is(StompHandler.Frame.CONNECTED));
        assertThat(response.headers, hasEntry("version", "1.1"));
    }

    @Test
    void givenStompFrame_whenProcessed_connectedFrameIsProduced() {
        StompHandler.Frame frame = new StompHandler.Frame(StompHandler.Frame.STOMP, new HashMap<>(), null);
        stompHandler.processFrame(frame, webSocketSession, frameConsumer);

        verify(frameConsumer).accept(frameCaptor.capture());
        StompHandler.Frame response = frameCaptor.getValue();
        assertThat(response.frameType, is(StompHandler.Frame.CONNECTED));
        assertThat(response.headers, hasEntry("version", "1.1"));
    }

    @Test
    void givenSubscribeFrame_whenProcessed_subscriptionIsCreated() {
        Map<String, String> headers = new HashMap<>();
        headers.put("id", "112233");
        headers.put("destination", SUBSCRIPTION_DESTINATION);
        StompHandler.Frame frame = new StompHandler.Frame(StompHandler.Frame.SUBSCRIBE, headers, null);
        stompHandler.processFrame(frame, webSocketSession, frameConsumer);

        assertThat(stompHandler.getSubscriptions(), hasKey(SUBSCRIPTION_DESTINATION));
        StompHandler.Subscription subscription = stompHandler.getSubscriptions().get(SUBSCRIPTION_DESTINATION);
        assertThat(subscription.id, is("112233"));
        assertThat(subscription.destination, is(SUBSCRIPTION_DESTINATION));
        assertThat(subscription.session, is(webSocketSession));
    }

    @Test
    void givenUnsubscribeFrame_whenProcessed_subscriptionIsRemoved() {
        stompHandler.getSubscriptions().put(SUBSCRIPTION_DESTINATION,
                new StompHandler.Subscription("112233", SUBSCRIPTION_DESTINATION, webSocketSession));
        Map<String, String> headers = new HashMap<>();
        headers.put("id", "112233");
        StompHandler.Frame frame = new StompHandler.Frame(StompHandler.Frame.UNSUBSCRIBE, headers, null);
        stompHandler.processFrame(frame, webSocketSession, frameConsumer);

        assertThat(stompHandler.getSubscriptions(), not(hasKey(SUBSCRIPTION_DESTINATION)));
    }

    @Test
    void givenAFrameWithReceipt_whenProcessed_receiptFrameIsProduced() {
        Map<String, String> headers = new HashMap<>();
        headers.put("receipt", "4567");
        StompHandler.Frame frame = new StompHandler.Frame(StompHandler.Frame.DISCONNECT, headers, null);
        stompHandler.processFrame(frame, webSocketSession, frameConsumer);

        verify(frameConsumer).accept(frameCaptor.capture());
        StompHandler.Frame response = frameCaptor.getValue();
        assertThat(response.frameType, is(StompHandler.Frame.RECEIPT));
        assertThat(response.headers, hasEntry("receipt-id", "4567"));
    }

    @Test
    void givenASubscriptionAndPayload_aValidMessgeFrameCanBeProduced() throws JsonProcessingException {
        String payload = "Hello";
        StompHandler.Subscription subscription = new StompHandler.Subscription("123", SUBSCRIPTION_DESTINATION, webSocketSession);
        StompHandler.Frame frame = stompHandler.createMessageFrame(subscription, payload);
        assertThat(frame.frameType, is(StompHandler.Frame.MESSAGE));
        assertThat(frame.headers, hasEntry("destination", SUBSCRIPTION_DESTINATION));
        assertThat(frame.headers, hasEntry("subscription", "123"));
        assertThat(frame.headers, hasEntry("content-type", "application/json"));
        assertThat(frame.headers, hasEntry("content-length", String.valueOf(("\"" + payload + "\"").getBytes().length)));
        assertThat(frame.headers, hasKey("message-id"));
    }

    @Test
    void when2MessagesAreProduced_thenTheirIdsDiffer() throws JsonProcessingException {
        String payload = "Hello";
        StompHandler.Subscription subscription = new StompHandler.Subscription("123", SUBSCRIPTION_DESTINATION, webSocketSession);
        StompHandler.Frame frame1 = stompHandler.createMessageFrame(subscription, payload);
        StompHandler.Frame frame2 = stompHandler.createMessageFrame(subscription, payload);

        assertThat(frame1.headers, hasKey("message-id"));
        assertThat(frame2.headers, hasKey("message-id"));
        assertThat(frame2.headers.get("message-id"), not(equalTo(frame1.headers.get("message-id"))));
    }
}