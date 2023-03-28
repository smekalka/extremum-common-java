package io.extremum.common.descriptor.factory;

import io.extremum.sharedmodels.annotation.UsesStaticDependencies;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.StorageType;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public final class DescriptorResolver {

    @UsesStaticDependencies
    public static String resolve(Descriptor descriptor, StorageType expectedType) {
        String internalId = descriptor.getInternalId();
        String currentType = descriptor.getStorageType();

        makeSureStorageTypesMatch(expectedType, currentType);

        return internalId;
    }

    private static void makeSureStorageTypesMatch(StorageType expectedType, String actualType) {
        if (!expectedType.matches(actualType)) {
            throw new IllegalStateException("Wrong descriptor storage type " + actualType);
        }
    }

    @UsesStaticDependencies
    public static Mono<String> resolveReactively(Descriptor descriptor, StorageType expectedType) {
        return descriptor.getStorageTypeReactively()
                .doOnNext(currentType -> makeSureStorageTypesMatch(expectedType, currentType))
                .then(descriptor.getInternalIdReactively());
    }

    private DescriptorResolver() {
    }
}
