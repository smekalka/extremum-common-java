package io.extremum.common.support;

import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

public interface UniversalReactiveModelLoaders {
    Mono<Model> loadByDescriptor(Descriptor descriptor);
}
