package io.extremum.watch.config.conditional;

import io.extremum.authentication.api.IdentityFinder;
import io.extremum.authentication.api.SecurityIdentity;
import io.extremum.common.reactive.Reactifier;
import io.extremum.security.ReactivePrincipalSource;
import io.extremum.watch.controller.ReactiveWebSocketHandler;
import io.extremum.watch.processor.ReactiveWatchEventNotificationSender;
import io.extremum.watch.processor.ReactiveWebSocketWatchEventNotificationSender;
import io.extremum.watch.processor.StompHandler;
import io.extremum.watch.processor.WatchEventNotificationSender;
import io.extremum.watch.services.ReactiveWatchSubscriberIdProvider;
import io.extremum.watch.services.ReactiveWatchSubscriptionService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConditionalOnProperty(prefix = "custom", value = "watch.reactive", havingValue = "true")
public class ReactiveWatchConfiguration {
    @Bean
    public ReactiveWatchEventNotificationSender watchEventNotificationSender(StompHandler stompHandler,
                                                                             ReactiveWatchSubscriptionService subscriptionService) {
        return new ReactiveWebSocketWatchEventNotificationSender(stompHandler, subscriptionService);
    }

    @Bean
    public WebSocketHandler handler(StompHandler stompHandler) {
        return new ReactiveWebSocketHandler(stompHandler);
    }

    @Bean
    public HandlerMapping handlerMapping(WebSocketHandler handler) {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/ws", handler);

        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(map);
        mapping.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return mapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter(webSocketService());
    }

    @Bean
    public WebSocketService webSocketService() {
        return new HandshakeWebSocketService(new ReactorNettyRequestUpgradeStrategy());
    }

    @Bean
    @ConditionalOnMissingBean
    ReactiveWatchSubscriberIdProvider reactiveSubscriberIdProvider(ReactivePrincipalSource principalSource,
                                                                   IdentityFinder identityFinder,
                                                                   Reactifier reactifier) {
        return () -> principalSource.getPrincipal()
                .flatMap(principalId -> reactifier.mono(
                        () -> identityFinder.findByPrincipalId(principalId.getName()).getExternalId()));
    }
}
