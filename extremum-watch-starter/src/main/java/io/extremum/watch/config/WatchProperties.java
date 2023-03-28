package io.extremum.watch.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "custom.watch")
@Getter
@Setter
public class WatchProperties {
    private int subscriptionTimeToLiveDays = 30;
    private int subscriptionIdleTimeDays = 7;
    private int processingThreads = 4;
}
