package io.extremum.watch.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("custom.kafka")
public class ExtremumKafkaProperties {
    private String server;
    private String topic;
}
