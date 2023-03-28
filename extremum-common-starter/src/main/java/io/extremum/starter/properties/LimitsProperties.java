package io.extremum.starter.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix = "extremum.limits")
public class LimitsProperties {
    private long collectionTopMaxSizeBytes = 2 * 1024 * 1024;
}
