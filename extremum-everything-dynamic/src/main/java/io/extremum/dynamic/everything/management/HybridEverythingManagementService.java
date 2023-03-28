package io.extremum.dynamic.everything.management;

import com.github.fge.jsonpatch.JsonPatch;
import io.extremum.dynamic.ReactiveDescriptorDeterminator;
import io.extremum.everything.services.management.ReactiveEverythingManagementService;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Getter
@RequiredArgsConstructor
public class HybridEverythingManagementService implements ReactiveEverythingManagementService {
    private final ReactiveEverythingManagementService defaultModelEverythingManagementService;
    private final ReactiveDynamicModelEverythingManagementService dynamicModelEverythingManagementService;
    private final ReactiveDescriptorDeterminator reactiveDescriptorDeterminator;

    @Override
    public Mono<ResponseDto> get(Descriptor id, boolean expand) {
        return findService(id).flatMap(service -> service.get(id, expand));
    }

    @Override
    public Mono<ResponseDto> patch(Descriptor id, JsonPatch patch, boolean expand) {
        return findService(id).flatMap(service -> service.patch(id, patch, expand));
    }

    @Override
    public Mono<Void> remove(Descriptor id) {
        return findService(id).flatMap(service -> service.remove(id));
    }

    private Mono<ReactiveEverythingManagementService> findService(Descriptor id) {
        return reactiveDescriptorDeterminator.isDynamic(id)
                .map(isDynamic -> {
                    if (isDynamic) {
                        log.debug("Descriptor {} determined as a descriptor for a dynamic-model and will be processed with {} service",
                                id, dynamicModelEverythingManagementService.getClass());
                        return dynamicModelEverythingManagementService;
                    } else {
                        log.debug("Descriptor {} determined as a descriptor for a standard-model and will be processed with {} service",
                                id, defaultModelEverythingManagementService.getClass());
                        return defaultModelEverythingManagementService;
                    }
                });
    }
}
