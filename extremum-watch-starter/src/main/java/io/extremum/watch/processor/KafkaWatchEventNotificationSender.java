package io.extremum.watch.processor;

import io.extremum.watch.config.ExtremumKafkaProperties;
import io.extremum.watch.dto.TextWatchEventNotificationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * @author rpuch
 */
@RequiredArgsConstructor
public class KafkaWatchEventNotificationSender implements WatchEventNotificationSender {
    private final ExtremumKafkaProperties kafkaProperties;
    private final KafkaTemplate<String, TextWatchEventNotificationDto> kafkaTemplate;

    @Override
    public void send(String modelId, TextWatchEventNotificationDto notificationDto) {
        kafkaTemplate.send(kafkaProperties.getTopic(), notificationDto);
    }
}
