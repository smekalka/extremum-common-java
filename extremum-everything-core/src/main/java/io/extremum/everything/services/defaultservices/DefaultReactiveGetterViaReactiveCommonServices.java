package io.extremum.everything.services.defaultservices;

import io.extremum.common.service.ReactiveCommonService;
import io.extremum.common.support.ReactiveCommonServices;
import io.extremum.everything.support.ReactiveModelDescriptors;
import io.extremum.sharedmodels.basic.Model;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class DefaultReactiveGetterViaReactiveCommonServices implements DefaultReactiveGetter {
    private final ReactiveCommonServices commonServices;
    private final ReactiveModelDescriptors modelDescriptors;

    @Override
    public Mono<Model> get(String internalId) {
        return findService(internalId)
                .flatMap(service -> service.get(internalId));
    }

    private Mono<ReactiveCommonService<Model>> findService(String internalId) {
        return modelDescriptors.getModelClassByModelInternalId(internalId)
                .map(commonServices::findServiceByModel);
    }
}
