package io.extremum.jpa.tx;

import io.extremum.sharedmodels.descriptor.StandardStorageType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JpaCollectionTransactorTest {
    @InjectMocks
    private JpaCollectionTransactor transactor;

    @Spy
    private TransactionOperations transactionOperations = new TestTransactionOperations();

    @Test
    void whenTypeIsQueried_thenPostgresShouldBeReturned() {
        assertThat(transactor.hostStorageType(), is(StandardStorageType.POSTGRES));
    }

    @Test
    void whenDoInTransactionIsInvoked_thenTheActionShoulBeInvokedAndYieldTheCorrectResult() {
        int result = transactor.doInTransaction(() -> 42);

        assertThat(result, is(42));
    }

    @Test
    void whenDoInTransactionIsInvoked_thenTheActionShoulBeInvokedInsideATransactionalTemplate() {
        transactor.doInTransaction(() -> 42);

        verify(transactionOperations).execute(any());
    }

    private static class TestTransactionOperations implements TransactionOperations {
        @Override
        public <T> T execute(TransactionCallback<T> action) throws TransactionException {
            return action.doInTransaction(mock(TransactionStatus.class));
        }
    }
}