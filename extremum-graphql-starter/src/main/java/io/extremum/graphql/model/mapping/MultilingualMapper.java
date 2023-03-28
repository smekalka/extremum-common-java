package io.extremum.graphql.model.mapping;

import com.github.dozermapper.core.CustomConverter;
import io.extremum.sharedmodels.basic.Multilingual;

public class MultilingualMapper implements CustomConverter {

    @Override
    public Object convert(Object existingDestinationFieldValue, Object sourceFieldValue, Class<?> destinationClass, Class<?> sourceClass) {
        Multilingual existing = (Multilingual) existingDestinationFieldValue;
        Multilingual source = (Multilingual) sourceFieldValue;
        existing.getMap().putAll(source.getMap());

        return existing;
    }
}