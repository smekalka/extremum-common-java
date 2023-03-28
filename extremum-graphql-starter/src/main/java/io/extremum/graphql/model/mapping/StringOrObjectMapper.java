package io.extremum.graphql.model.mapping;


import com.github.dozermapper.core.CustomConverter;
import com.github.dozermapper.core.DozerBeanMapperBuilder;
import com.github.dozermapper.core.Mapper;

public class StringOrObjectMapper implements CustomConverter {

    @Override
    public Object convert(Object existingDestinationFieldValue, Object sourceFieldValue, Class<?> destinationClass, Class<?> sourceClass) {
        Mapper mapper = DozerBeanMapperBuilder
                .create()
                .withCustomFieldMapper((source, destination, o, classMap, fieldMapping) -> o == null)
                .build();
        if (existingDestinationFieldValue == null) {
            return sourceFieldValue;
        }
        mapper.map(sourceFieldValue, existingDestinationFieldValue);

        return existingDestinationFieldValue;
    }
}
