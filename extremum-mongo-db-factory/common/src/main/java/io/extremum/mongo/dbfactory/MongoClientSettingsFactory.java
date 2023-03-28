package io.extremum.mongo.dbfactory;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import org.bson.UuidRepresentation;

public class MongoClientSettingsFactory {
    public static MongoClientSettings standardSettings(String uri) {
        return MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .build();
    }

    private MongoClientSettingsFactory() {}
}
