package io.extremum.everything.services.defaultservices;

import io.extremum.common.support.ReactiveCommonServices;
import io.extremum.everything.support.ReactiveModelDescriptors;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class DefaultReactiveRemoverImpl implements DefaultReactiveRemover {
    private final ReactiveCommonServices commonServices;
    private final ReactiveModelDescriptors modelDescriptors;

    @Override
    public Mono<Void> remove(String id) {
        return modelDescriptors.getModelClassByModelInternalId(id)
                .map(commonServices::findServiceByModel)
                .flatMap(service -> service.delete(id))
                .then();
    }

}
