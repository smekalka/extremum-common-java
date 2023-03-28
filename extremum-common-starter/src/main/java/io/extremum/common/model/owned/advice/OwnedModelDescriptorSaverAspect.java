package io.extremum.common.model.owned.advice;

import io.extremum.common.collection.service.ReactiveOwnedModelDescriptorService;
import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.common.model.advice.ReactiveModelLifecycleAspect;
import io.extremum.common.model.owned.OwnedModelLifecycleSupport;
import io.extremum.common.model.owned.model.OwnedModel;
import io.extremum.sharedmodels.descriptor.OwnedCoordinates;
import io.extremum.sharedmodels.descriptor.OwnedModelCoordinates;
import io.extremum.sharedmodels.descriptor.OwnedModelDescriptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class OwnedModelDescriptorSaverAspect extends ReactiveModelLifecycleAspect<BasicModel<?>> {

    private final OwnedModelLifecycleSupport ownedModelLifecycleSupport;
    private final ReactiveOwnedModelDescriptorService reactiveOwnedModelDescriptorService;

    @Override
    protected Mono<Void> onAfterModelSaved(BasicModel<?> model) {
        return Flux.fromIterable(ownedModelLifecycleSupport.getOwnedFields(model)).flatMap(
                field -> {
                    field.setAccessible(true);
                    OwnedModel ownedModelValue = null;
                    try {
                        ownedModelValue = (OwnedModel) field.get(model);
                    } catch (IllegalAccessException e) {
                        return Mono.error(e);
                    }
                    if (ownedModelValue.getUuid() == null) {
                        log.info("Create descriptor for owned model");

                    }
                    OwnedModelDescriptor ownedModelDescriptor = new OwnedModelDescriptor(
                            new OwnedModelCoordinates(new OwnedCoordinates(
                                    model.getUuid(), field.getName()
                            ))
                    );

                    field.setAccessible(false);
                    return reactiveOwnedModelDescriptorService.retrieveByCoordinatesOrCreate(ownedModelDescriptor);
                }
        ).then();
    }

    @Override
    protected Mono<Void> onAfterModelSaved(BasicModel<?> nested, BasicModel<?> arg) {
        return Mono.empty().then();
    }

    @Override
    protected void onBeforeModelSaved(BasicModel<?> nested, BasicModel<?> arg) {

    }

    @Override
    protected void onBeforeModelSaved(BasicModel<?> model) {

    }
}
