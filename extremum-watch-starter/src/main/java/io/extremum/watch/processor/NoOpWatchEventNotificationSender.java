package io.extremum.watch.processor;

import io.extremum.watch.dto.TextWatchEventNotificationDto;
import org.springframework.stereotype.Service;

/**
 * @author rpuch
 */
// TODO remove? @Service
public class NoOpWatchEventNotificationSender implements WatchEventNotificationSender {
    @Override
    public void send(String modelId, TextWatchEventNotificationDto notificationDto) {
        // doing nothing
    }
}
