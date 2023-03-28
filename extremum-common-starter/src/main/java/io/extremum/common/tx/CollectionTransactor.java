package io.extremum.common.tx;

import io.extremum.sharedmodels.descriptor.StorageType;

import java.util.function.Supplier;

/**
 * Transactor used when streaming a collection. It is meant to wrap the whole
 * database-accessing code in a transaction.
 * Currently, it is only needed for JPA.
 */
public interface CollectionTransactor {
    StorageType hostStorageType();

    <T> T doInTransaction(Supplier<T> action);
}
