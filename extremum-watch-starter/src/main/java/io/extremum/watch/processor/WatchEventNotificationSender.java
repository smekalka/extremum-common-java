package io.extremum.watch.processor;

import io.extremum.watch.dto.TextWatchEventNotificationDto;

/**
 * @author rpuch
 */
public interface WatchEventNotificationSender {
    void send(String modelId, TextWatchEventNotificationDto notificationDto);
}
