package io.extremum.watch.processor;

import io.extremum.watch.dto.TextWatchEventNotificationDto;
import io.extremum.watch.dto.WebSocketNotificationDto;
import io.extremum.watch.services.WatchSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@RequiredArgsConstructor
public class WebSocketWatchEventNotificationSender implements WatchEventNotificationSender {
    private final SimpMessagingTemplate messagingTemplate;
    private final WatchSubscriptionService subscriptionService;

    protected String getJsonPatch(String modelId, String subscriberId, TextWatchEventNotificationDto notificationDto) {
        if (subscriptionService.isFreshSubscription(modelId, subscriberId) && notificationDto.getFullReplacePatch() != null) {
            return notificationDto.getFullReplacePatch();
        } else {
            return notificationDto.getJsonPatch();
        }
    }

    @Override
    public void send(String modelId, TextWatchEventNotificationDto notificationDto) {
        for (String subscriberId: notificationDto.getSubscribers()) {
            String jsonPatch = getJsonPatch(modelId, subscriberId, notificationDto);
            messagingTemplate.convertAndSendToUser(subscriberId, "/watch", new WebSocketNotificationDto(jsonPatch));
        }
    }
}
