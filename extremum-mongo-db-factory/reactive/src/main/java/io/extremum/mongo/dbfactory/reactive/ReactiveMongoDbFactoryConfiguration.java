package io.extremum.mongo.dbfactory.reactive;

import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import io.extremum.mongo.dbfactory.MainMongoDb;
import io.extremum.mongo.dbfactory.MongoClientSettingsFactory;
import io.extremum.mongo.dbfactory.MongoDatabaseFactoryProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;

@Configuration
@EnableConfigurationProperties(MongoDatabaseFactoryProperties.class)
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "mongo", value = "uri")
public class ReactiveMongoDbFactoryConfiguration {
    private final MongoDatabaseFactoryProperties mongoDatabaseFactoryProperties;

    @Bean
    public MongoClient reactiveMongoClient() {
        MongoClientSettings settings = MongoClientSettingsFactory.standardSettings(
                mongoDatabaseFactoryProperties.getUri());
        return MongoClients.create(settings);
    }

    @Bean
    @MainMongoDb
    public ReactiveMongoDatabaseFactory reactiveMongoDbFactory() {
        return new SimpleReactiveMongoDatabaseFactory(reactiveMongoClient(), getDatabaseName());
    }

    private String getDatabaseName() {
        return mongoDatabaseFactoryProperties.getServiceDbName();
    }
}
