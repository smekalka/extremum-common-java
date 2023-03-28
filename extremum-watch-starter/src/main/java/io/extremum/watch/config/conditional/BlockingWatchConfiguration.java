package io.extremum.watch.config.conditional;

import io.extremum.authentication.api.IdentityFinder;
import io.extremum.authentication.api.SecurityIdentity;
import io.extremum.security.PrincipalSource;
import io.extremum.watch.processor.WatchEventNotificationSender;
import io.extremum.watch.processor.WebSocketWatchEventNotificationSender;
import io.extremum.watch.services.WatchSubscriberIdProvider;
import io.extremum.watch.services.WatchSubscriptionService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.security.Principal;

@Configuration
public class BlockingWatchConfiguration {
    @Bean
    WatchEventNotificationSender watchEventNotificationSender(SimpMessagingTemplate messagingTemplate,
                                                              WatchSubscriptionService watchSubscriptionService) {
        return new WebSocketWatchEventNotificationSender(messagingTemplate, watchSubscriptionService);
    }

    @Bean
    @ConditionalOnMissingBean
    WatchSubscriberIdProvider subscriberIdProvider(PrincipalSource principalSource, IdentityFinder identityFinder) {
        return () -> principalSource.getPrincipal()
                .map((Principal principalId) -> identityFinder.findByPrincipalId(principalId.getName()))
                .map(SecurityIdentity::getExternalId);
    }
}

