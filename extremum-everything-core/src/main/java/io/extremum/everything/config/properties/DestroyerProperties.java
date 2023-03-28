package io.extremum.everything.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "custom.field-destroyer")
public class DestroyerProperties {
    private List<String> analyzablePackagePrefix = new ArrayList<>();
}
