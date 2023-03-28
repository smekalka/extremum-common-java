package io.extremum.watch.config.conditional;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.watch.config.ExtremumKafkaProperties;
import io.extremum.watch.dto.TextWatchEventNotificationDto;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

import static org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(ExtremumKafkaProperties.class)
@ConditionalOnProperty(prefix = "custom", value = "watch.kafka", havingValue = "true")
public class KafkaConfiguration {
    private final ObjectMapper objectMapper;
    private final ExtremumKafkaProperties kafkaProperties;

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getServer());
        props.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return props;
    }

    @Bean
    public ProducerFactory<String, TextWatchEventNotificationDto> producerWatchFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, TextWatchEventNotificationDto> watchKafkaTemplate() {
        KafkaTemplate<String, TextWatchEventNotificationDto> kafkaTemplate = new KafkaTemplate<>(producerWatchFactory());
        kafkaTemplate.setMessageConverter(new StringJsonMessageConverter(objectMapper));
        return kafkaTemplate;
    }
}