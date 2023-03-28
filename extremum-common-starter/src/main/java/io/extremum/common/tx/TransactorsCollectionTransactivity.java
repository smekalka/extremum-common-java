package io.extremum.common.tx;

import com.google.common.collect.ImmutableList;
import io.extremum.sharedmodels.descriptor.Descriptor;

import java.util.List;
import java.util.function.Supplier;

public class TransactorsCollectionTransactivity implements CollectionTransactivity {
    private final List<CollectionTransactor> transactors;

    public TransactorsCollectionTransactivity(List<CollectionTransactor> transactors) {
        this.transactors = ImmutableList.copyOf(transactors);
    }

    @Override
    public boolean isCollectionTransactional(Descriptor hostId) {
        return transactors.stream()
                .anyMatch(transactor -> transactor.hostStorageType().matches(hostId.getStorageType()));
    }

    @Override
    public <T> T doInTransaction(Descriptor hostId, Supplier<T> action) {
        CollectionTransactor transactor = requiredTransactor(hostId);
        return transactor.doInTransaction(action);
    }

    private CollectionTransactor requiredTransactor(Descriptor hostId) {
        return transactors.stream()
                    .filter(ttor -> ttor.hostStorageType().matches(hostId.getStorageType()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            String.format("Did not find any transactor for '%s'", hostId.getStorageType())));
    }
}
