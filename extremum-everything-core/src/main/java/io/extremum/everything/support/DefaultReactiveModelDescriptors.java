package io.extremum.everything.support;

import io.extremum.common.descriptor.service.ReactiveDescriptorService;
import io.extremum.common.support.ModelClasses;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.DescriptorNotFoundException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class DefaultReactiveModelDescriptors implements ReactiveModelDescriptors {
    private final ModelClasses modelClasses;
    private final ReactiveDescriptorService descriptorService;

    @Override
    public <M extends Model> Mono<Class<M>> getModelClassByModelInternalId(String internalId) {
        return descriptorService.loadByInternalId(internalId)
                .switchIfEmpty(Mono.error(new DescriptorNotFoundException("For internal id: " + internalId, Descriptor.builder().internalId(internalId).build())))
                .map(descriptor -> modelClasses.getClassByModelName(descriptor.getModelType()));
    }
}
