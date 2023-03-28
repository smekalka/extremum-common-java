package io.extremum.dynamic.metadata;

import io.extremum.dynamic.models.DynamicModel;

public interface MetadataProviderService {
    <T> void provideMetadata(DynamicModel<T> model);
}
