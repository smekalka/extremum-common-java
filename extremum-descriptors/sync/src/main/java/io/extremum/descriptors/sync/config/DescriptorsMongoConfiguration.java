package io.extremum.descriptors.sync.config;

import com.mongodb.WriteConcern;
import com.mongodb.client.MongoDatabase;
import io.extremum.descriptors.sync.lifecycle.CollectionDescriptorCoordinatesRefresher;
import io.extremum.descriptors.common.CommonDescriptorsMongoConfiguration;
import io.extremum.descriptors.common.DescriptorsMongoDb;
import io.extremum.descriptors.common.dao.DescriptorRepository;
import io.extremum.descriptors.common.properties.DescriptorsMongoProperties;
import io.extremum.descriptors.sync.dao.impl.SpringDataDescriptorRepository;
import io.extremum.mongo.dbfactory.MainMongoDb;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mapping.callback.EntityCallbacks;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoDatabaseUtils;
import org.springframework.data.mongodb.SessionSynchronization;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.event.AuditingEntityCallback;

/**
 * @author rpuch
 */
@RequiredArgsConstructor
@Configuration
@EnableConfigurationProperties(DescriptorsMongoProperties.class)
@Import(CommonDescriptorsMongoConfiguration.class)
@ConditionalOnProperty(prefix = "mongo", value = "uri")
public class DescriptorsMongoConfiguration {
    private final DescriptorsMongoProperties descriptorsMongoProperties;

    @Bean
    @DescriptorsMongoDb
    public MongoTemplate descriptorsMongoTemplate(@MainMongoDb MongoDatabaseFactory mainMongoDbFactory,
            @DescriptorsMongoDb MappingMongoConverter descriptorsMappingMongoConverter,
            AuditingEntityCallback auditingEntityCallback,
            CollectionDescriptorCoordinatesRefresher collectionDescriptorCoordinatesRefresher) {
        MongoTemplate template = new MongoTemplate(mainMongoDbFactory, descriptorsMappingMongoConverter) {
            @Override
            protected MongoDatabase doGetDatabase() {
                return MongoDatabaseUtils.getDatabase(getDatabaseName(), mainMongoDbFactory,
                        SessionSynchronization.ON_ACTUAL_TRANSACTION);
            }
        };
        template.setWriteResultChecking(WriteResultChecking.EXCEPTION);
        template.setWriteConcern(WriteConcern.MAJORITY);
        template.setEntityCallbacks(explicitCallbacksToAvoidCircularDependencyProblem(
                auditingEntityCallback, collectionDescriptorCoordinatesRefresher));
        return template;
    }

    private String getDatabaseName() {
        return descriptorsMongoProperties.getDescriptorsDbName();
    }

    private EntityCallbacks explicitCallbacksToAvoidCircularDependencyProblem(
            AuditingEntityCallback auditingEntityCallback,
            CollectionDescriptorCoordinatesRefresher collectionDescriptorCoordinatesRefresher) {
        // we have to construct this explicitly because otherwise any EntityCallback that depends on descriptor-related
        // beans (like the ones that support CommonModel descriptor-related filling/resolving) would create an
        // unresolvable circular dependency during startup
        return EntityCallbacks.create(auditingEntityCallback, collectionDescriptorCoordinatesRefresher);
    }

    @Bean
    public DescriptorRepository descriptorRepository(@DescriptorsMongoDb MongoOperations descriptorsMongoOperations) {
        return new SpringDataDescriptorRepository(descriptorsMongoOperations);
    }

    @Bean
    public CollectionDescriptorCoordinatesRefresher collectionDescriptorCoordinatesStringRefresher() {
        return new CollectionDescriptorCoordinatesRefresher();
    }
}
