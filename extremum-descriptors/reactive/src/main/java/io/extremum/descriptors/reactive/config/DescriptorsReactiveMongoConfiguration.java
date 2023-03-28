package io.extremum.descriptors.reactive.config;

import com.mongodb.WriteConcern;
import com.mongodb.reactivestreams.client.MongoDatabase;
import io.extremum.descriptors.common.CommonDescriptorsMongoConfiguration;
import io.extremum.descriptors.common.DescriptorsMongoDb;
import io.extremum.descriptors.common.properties.DescriptorsMongoProperties;
import io.extremum.descriptors.reactive.lifecycle.ReactiveCollectionDescriptorCoordinatesRefresher;
import io.extremum.mongo.dbfactory.MainMongoDb;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mapping.callback.ReactiveEntityCallbacks;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.ReactiveMongoDatabaseUtils;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.event.ReactiveAuditingEntityCallback;
import reactor.core.publisher.Mono;

import static org.springframework.data.mongodb.SessionSynchronization.ON_ACTUAL_TRANSACTION;

@Configuration
@EnableConfigurationProperties(DescriptorsMongoProperties.class)
@Import(CommonDescriptorsMongoConfiguration.class)
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "mongo", value = "uri")
public class DescriptorsReactiveMongoConfiguration {
    private final DescriptorsMongoProperties descriptorsMongoProperties;

    private String getDatabaseName() {
        return descriptorsMongoProperties.getDescriptorsDbName();
    }

    @Bean
    @DescriptorsMongoDb
    public ReactiveMongoOperations descriptorsReactiveMongoTemplate(
            @MainMongoDb ReactiveMongoDatabaseFactory mainReactiveMongoDatabaseFactory,
            @DescriptorsMongoDb MappingMongoConverter mappingMongoConverter,
            ReactiveAuditingEntityCallback auditingEntityCallback,
            ReactiveCollectionDescriptorCoordinatesRefresher refresher) {
        ReactiveMongoTemplate template = new ReactiveMongoTemplate(mainReactiveMongoDatabaseFactory,
                mappingMongoConverter) {
            // Method redefinition is required to make sure we use the correct database.
            // Currently, spring-data-mongodb uses ReactiveMongoDatabaseFactory as a transactional resource, so we
            // must have only one such factory, but by default ReactiveMongoTemplate uses the database configured
            // on the factory. That's why we have to specifically switch to the needed database.

            @Override
            protected Mono<MongoDatabase> doGetDatabase() {
                return ReactiveMongoDatabaseUtils.getDatabase(getDatabaseName(), mainReactiveMongoDatabaseFactory,
                        ON_ACTUAL_TRANSACTION);
            }

            @Override
            public Mono<MongoDatabase> getMongoDatabase() {
                return mainReactiveMongoDatabaseFactory.getMongoDatabase(getDatabaseName());
            }
        };
        template.setWriteResultChecking(WriteResultChecking.EXCEPTION);
        template.setWriteConcern(WriteConcern.MAJORITY);
        template.setEntityCallbacks(
                explicitCallbacksToAvoidCircularDependencyProblem(auditingEntityCallback, refresher));
        return template;
    }

    private ReactiveEntityCallbacks explicitCallbacksToAvoidCircularDependencyProblem(
            ReactiveAuditingEntityCallback auditingEntityCallback,
            ReactiveCollectionDescriptorCoordinatesRefresher refresher) {
        // we have to construct this explicitly because otherwise any EntityCallback that depends on descriptor-related
        // beans (like the ones that support CommonModel descriptor-related filling/resolving) would create an
        // unresolvable circular dependency during startup
        return ReactiveEntityCallbacks.create(auditingEntityCallback, refresher);
    }

    @Bean
    public ReactiveCollectionDescriptorCoordinatesRefresher reactiveCollectionDescriptorCoordinatesRefresher() {
        return new ReactiveCollectionDescriptorCoordinatesRefresher();
    }
}
