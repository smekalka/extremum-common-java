package io.extremum.common.lifecycle;

import io.extremum.common.facilities.ReactiveDescriptorFacilities;
import io.extremum.sharedmodels.basic.HasUuid;
import io.extremum.common.utils.ModelUtils;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
@RequiredArgsConstructor
public final class ReactiveCommonModelLifecycleSupport<T extends HasUuid> {
    private final ReactiveDescriptorFacilities descriptorFacilities;
    private final InternalIdAdapter<? super T> adapter;

    public Mono<Void> fillRequiredFields(T model) {
        return Mono.defer(() -> {
            final boolean internalIdGiven = adapter.getInternalId(model).isPresent();
            final boolean uuidGiven = model.getUuid() != null;

            if (uuidGiven && !internalIdGiven) {
                return getInternalIdFromDescriptor(model)
                        .doOnNext(internalId -> adapter.setInternalId(model, internalId))
                        .then();
            } else if (!uuidGiven && internalIdGiven) {
                return createAndSaveDescriptorWithGivenInternalId(adapter.getInternalId(model).get(), model)
                        .doOnNext(model::setUuid)
                        .then();
            } else if (!uuidGiven && !internalIdGiven) {
                return createAndSaveDescriptorWithGivenInternalId(adapter.generateNewInternalId(), model)
                        .doOnNext(model::setUuid)
                        .then(getInternalIdFromDescriptor(model))
                        .doOnNext(internalId -> adapter.setInternalId(model, internalId))
                        .then();
            }

            return Mono.empty();
        });
    }

    private Mono<String> getInternalIdFromDescriptor(T model) {
        return Mono.fromSupplier(model::getUuid)
                .flatMap(descriptorFacilities::resolve);
    }

    private Mono<Descriptor> createAndSaveDescriptorWithGivenInternalId(String internalId, T model) {
        return Mono.defer(() -> {
            String modelName = ModelUtils.getModelName(model);
            String iri = model.getIri();
            return descriptorFacilities.createOrGet(internalId, modelName, iri);
        });
    }

    public Mono<Void> createDescriptorIfNeeded(T model) {
        return Mono.defer(() -> {
            if (model.getUuid() == null) {
                String name = ModelUtils.getModelName(model.getClass());
                String iri = model.getIri();
                return descriptorFacilities.createOrGet(adapter.requiredInternalId(model), name, iri)
                        .doOnNext(model::setUuid)
                        .then();
            }

            return Mono.empty();
        });
    }

    public Mono<Void> fillDescriptorFromInternalId(T model) {
        return Mono.fromSupplier(() -> adapter.requiredInternalId(model))
                .flatMap(descriptorFacilities::fromInternalId)
                .doOnNext(model::setUuid)
                .then();
    }
}
