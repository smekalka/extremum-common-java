package common.dao.mongo.versioned;

import io.extremum.starter.CommonConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;

@Configuration
@Import(CommonConfiguration.class)
public class ReactiveVersionedMongoDaoTestConfiguration {
    @Bean
    public TestReactiveMongoVersionedDao testMongoVersionedDao(ReactiveMongoOperations reactiveMongoOperations) {
        return new TestReactiveMongoVersionedDaoImpl(reactiveMongoOperations);
    }
}
