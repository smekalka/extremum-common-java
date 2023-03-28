package io.extremum.mongo.dbfactory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties("mongo")
public class MongoDatabaseFactoryProperties {
    private String uri;
    // TODO: move this to MongoProperties
    private String serviceDbName;
}