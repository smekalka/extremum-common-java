package io.extremum.jpa.tx;

import io.extremum.common.tx.CollectionTransactor;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import io.extremum.sharedmodels.descriptor.StorageType;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.support.TransactionOperations;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class JpaCollectionTransactor implements CollectionTransactor {
    private final TransactionOperations transactionOperations;

    @Override
    public StorageType hostStorageType() {
        return StandardStorageType.POSTGRES;
    }

    @Override
    public <T> T doInTransaction(Supplier<T> action) {
        return transactionOperations.execute(status -> action.get());
    }
}
