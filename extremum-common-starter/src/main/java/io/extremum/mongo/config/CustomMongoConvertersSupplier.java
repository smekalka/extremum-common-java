package io.extremum.mongo.config;

import java.util.List;

/**
 * Used to add more custom converters to {@link org.springframework.data.mongodb.core.convert.MongoCustomConversions}
 * configured by Extremum.
 * It should return converters of same types that MongoCustomConversions accepts via its constructor.
 * The intended usage is that you define a bean of this type in your application context, and
 * Extremum code that constructs MongoCustomConversions picks it up automatically and adds its converters.
 */
@FunctionalInterface
public interface CustomMongoConvertersSupplier {
    List<Object> getConverters();
}
