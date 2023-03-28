package io.extremum.mongo.dbfactory.sync;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.extremum.mongo.dbfactory.MainMongoDb;
import io.extremum.mongo.dbfactory.MongoClientSettingsFactory;
import io.extremum.mongo.dbfactory.MongoDatabaseFactoryProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@Configuration
@EnableConfigurationProperties(MongoDatabaseFactoryProperties.class)
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "mongo", value = "uri")
public class MongoDbFactoryConfiguration {
    private final MongoDatabaseFactoryProperties mongoDatabaseFactoryProperties;

    @Bean
    @MainMongoDb
    public MongoClient mongoClient() {
        MongoClientSettings settings = MongoClientSettingsFactory.standardSettings(
                mongoDatabaseFactoryProperties.getUri());
        return MongoClients.create(settings);
    }

    @Bean
    @MainMongoDb
    public MongoDatabaseFactory mongoDbFactory() {
        return new SimpleMongoClientDatabaseFactory(mongoClient(), getDatabaseName());
    }

    private String getDatabaseName() {
        return mongoDatabaseFactoryProperties.getServiceDbName();
    }
}
