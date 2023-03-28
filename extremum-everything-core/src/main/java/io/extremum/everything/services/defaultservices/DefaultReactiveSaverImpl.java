package io.extremum.everything.services.defaultservices;

import io.extremum.common.service.ReactiveCommonService;
import io.extremum.common.support.ReactiveCommonServices;
import io.extremum.sharedmodels.basic.Model;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class DefaultReactiveSaverImpl implements DefaultReactiveSaver {
    private final ReactiveCommonServices commonServices;

    @Override
    public Mono<Model> save(Model model) {
        ReactiveCommonService<Model> service = commonServices.findServiceByModel(model.getClass());
        return service.save(model);
    }
}
