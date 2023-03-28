package io.extremum.jpa.config;

import io.extremum.starter.CommonConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({JpaRepositoriesConfiguration.class, CommonConfiguration.class})
public class EverythingConfiguration {
}
