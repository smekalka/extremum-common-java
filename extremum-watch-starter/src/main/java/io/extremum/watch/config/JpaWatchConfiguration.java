package io.extremum.watch.config;

import io.extremum.watch.config.conditional.BlockingWatchConfiguration;
import io.extremum.watch.repositories.jpa.JpaTextWatchEventRepository;
import io.extremum.watch.services.JpaTextWatchEventService;
import io.extremum.watch.services.TextWatchEventService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories("io.extremum.watch.repositories.jpa")
@ConditionalOnProperty(prefix = "extremum", value = "watch.storage.type", havingValue = "jpa")
@ConditionalOnBean(BlockingWatchConfiguration.class)
public class JpaWatchConfiguration {

    @Bean
    public TextWatchEventService textWatchEventService(JpaTextWatchEventRepository repository) {
        return new JpaTextWatchEventService(repository);
    }
}
