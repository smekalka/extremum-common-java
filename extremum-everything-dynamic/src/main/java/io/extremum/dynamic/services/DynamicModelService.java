package io.extremum.dynamic.services;

import io.extremum.dynamic.models.DynamicModel;
import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

public interface DynamicModelService<Model extends DynamicModel<?>> {
    Mono<Model> saveModel(Model model);

    Mono<Model> findById(Descriptor id);

    Mono<Model> remove(Descriptor descriptor);
}
