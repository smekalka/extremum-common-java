package io.extremum.watch.config;

import io.extremum.watch.config.conditional.BlockingWatchConfiguration;
import io.extremum.watch.repositories.MongoTextWatchEventRepository;
import io.extremum.watch.services.MongoTextWatchEventServiceImpl;
import io.extremum.watch.services.TextWatchEventService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableMongoRepositories(value = "io.extremum.watch.repositories",   excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "io\\.extremum\\.watch\\.repositories\\.jpa\\..*"
))
@ConditionalOnProperty(prefix = "extremum", value = "watch.storage.type", havingValue = "mongo")
@ConditionalOnBean(BlockingWatchConfiguration.class)
public class MongoWatchConfiguration {

    @Bean
    public TextWatchEventService textWatchEventService(MongoTextWatchEventRepository mongoTextWatchEventRepository) {
        return new MongoTextWatchEventServiceImpl(mongoTextWatchEventRepository);
    }
}
