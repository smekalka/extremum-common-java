package io.extremum.batch.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "batch.web.client")
public class WebClientProperties {
    private int workerThreadSize = 10;
}
