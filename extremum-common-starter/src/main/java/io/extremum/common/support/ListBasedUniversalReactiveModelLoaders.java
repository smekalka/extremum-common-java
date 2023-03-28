package io.extremum.common.support;

import com.google.common.collect.ImmutableList;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

import java.util.List;

public class ListBasedUniversalReactiveModelLoaders implements UniversalReactiveModelLoaders {
    private final List<UniversalReactiveModelLoader> loaders;
    private final ModelClasses modelClasses;

    public ListBasedUniversalReactiveModelLoaders(List<UniversalReactiveModelLoader> loaders,
                                                  ModelClasses modelClasses) {
        this.loaders = ImmutableList.copyOf(loaders);
        this.modelClasses = modelClasses;
    }

    @Override
    public Mono<Model> loadByDescriptor(Descriptor descriptor) {
        UniversalReactiveModelLoader loader = findLoader(descriptor);
        Class<Model> modelClass = modelClasses.getClassByModelName(descriptor.getModelType());

        return descriptor.getInternalIdReactively()
                .flatMap(internalId -> loader.loadByInternalId(descriptor.getInternalId(), modelClass));
    }

    private UniversalReactiveModelLoader findLoader(Descriptor descriptor) {
        return loaders.stream()
                .filter(loader -> loader.type().matches(descriptor.getStorageType()))
                .findAny()
                .orElseThrow(() -> new IllegalStateException(noLoaderAvailableMessage(descriptor)));
    }

    private String noLoaderAvailableMessage(Descriptor descriptor) {
        return String.format("No loader supports storage type '%s'. Make sure you have an instance of " +
                        "UniversalReactiveModelLoader that supports '%s' in the application context.",
                descriptor.getStorageType(), descriptor.getStorageType());
    }
}
