package io.extremum.common.tx;

import io.extremum.sharedmodels.descriptor.Descriptor;

import java.util.function.Supplier;

/**
 * Transactional concerns relating to collection streaming.
 */
public interface CollectionTransactivity {
    boolean isCollectionTransactional(Descriptor hostId);

    <T> T doInTransaction(Descriptor hostId, Supplier<T> action);
}
