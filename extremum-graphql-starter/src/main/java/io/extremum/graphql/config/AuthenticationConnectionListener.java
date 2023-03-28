package io.extremum.graphql.config;

import graphql.kickstart.execution.subscriptions.SubscriptionSession;
import graphql.kickstart.execution.subscriptions.apollo.ApolloSubscriptionConnectionListener;
import graphql.kickstart.execution.subscriptions.apollo.OperationMessage;
import graphql.kickstart.servlet.apollo.ApolloWebSocketSubscriptionSession;
import io.extremum.security.model.jwt.AuthenticationToken;
import io.extremum.security.model.jwt.OidcJwtTokenConverter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import javax.websocket.Session;
import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
class AuthenticationConnectionListener implements ApolloSubscriptionConnectionListener {

    private final JwtDecoder decoder;

    private final OidcJwtTokenConverter converter;

    public void onConnect(SubscriptionSession session, OperationMessage message) {
        log.debug("onConnect with payload {}", message.getPayload().getClass());
        Session unwrapped = ((ApolloWebSocketSubscriptionSession) session).unwrap();
        Map<String, List<String>> requestParameterMap = unwrapped.getRequestParameterMap();
        String accessToken = requestParameterMap.get("access_token").get(0);
        Jwt convert = decoder.decode(accessToken);
        AuthenticationToken authenticationToken = converter.convert(convert);
        log.debug("Token: {}", authenticationToken);
        session.getUserProperties().put("CONNECT_TOKEN", authenticationToken);

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}