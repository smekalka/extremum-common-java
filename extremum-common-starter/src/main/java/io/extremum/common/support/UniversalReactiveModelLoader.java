package io.extremum.common.support;

import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.StorageType;
import reactor.core.publisher.Mono;

public interface UniversalReactiveModelLoader {
    Mono<Model> loadByInternalId(String internalId, Class<? extends Model> modelClass);

    StorageType type();
}
