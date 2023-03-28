package io.extremum.mongo.config;

import org.springframework.data.convert.MappingContextTypeInformationMapper;
import org.springframework.data.mongodb.core.convert.*;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.util.Collections;

public class MappingMongoConverters {
    public static MappingMongoConverter aliasingConverter(MongoMappingContext mongoMappingContext,
            MongoCustomConversions customConversions, DbRefResolver dbRefResolver) {
        MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver,
                mongoMappingContext);
        converter.setCustomConversions(customConversions);

        // changing type mapper so that:
        // 1. if there is no @TypeAlias on the model class, _class attribute is not saved
        // 2. if @TypeAlias is there, its value is saved in _class attribute
        MappingContextTypeInformationMapper typeInformationMapper = new MappingContextTypeInformationMapper(
                mongoMappingContext);
        DefaultMongoTypeMapper typeMapper = new DefaultMongoTypeMapper(DefaultMongoTypeMapper.DEFAULT_TYPE_KEY,
                Collections.singletonList(typeInformationMapper));
        converter.setTypeMapper(typeMapper);

        return converter;
    }

    public static MappingMongoConverter aliasingConverterWithNoOpDbRefResolver(
            MongoMappingContext mongoMappingContext, MongoCustomConversions customConversions) {
        return aliasingConverter(mongoMappingContext, customConversions, NoOpDbRefResolver.INSTANCE);
    }
}
