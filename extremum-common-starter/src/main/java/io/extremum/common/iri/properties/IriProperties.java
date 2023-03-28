package io.extremum.common.iri.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "iri")
@Data
public class IriProperties {
    String base;
    String skolemPrefix = "genid";
}
