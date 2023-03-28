package io.extremum.common.lifecycle;

import java.util.Optional;

public interface InternalIdAdapter<T> {
    Optional<String> getInternalId(T model);

    default String requiredInternalId(T model) {
        return getInternalId(model)
                .orElseThrow(() -> new IllegalStateException("No internalId supplied"));
    }

    void setInternalId(T model, String internalId);

    String generateNewInternalId();
}
