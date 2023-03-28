package io.extremum.common.tx;

import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import io.extremum.sharedmodels.descriptor.StorageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Supplier;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactorsCollectionTransactivityTest {
    private TransactorsCollectionTransactivity transactivity;

    @Spy
    private CollectionTransactor transactor = new TestTransactor();

    private final Descriptor descriptor = Descriptor.builder()
            .storageType(StandardStorageType.POSTGRES)
            .build();

    @BeforeEach
    void createTransactivity() {
        transactivity = new TransactorsCollectionTransactivity(singletonList(transactor));
    }

    @Test
    void givenTheOnlyTransactorSupportsTheDescriptor_whenCheckingWhetherCollectionIsTransactional_thenShouldReturnTrue() {
        supportTheCurrentDescriptor();

        assertThat(transactivity.isCollectionTransactional(descriptor), is(true));
    }

    private void supportTheCurrentDescriptor() {
        when(transactor.hostStorageType()).thenReturn(StandardStorageType.POSTGRES);
    }

    @Test
    void givenTheOnlyTransactorDoesNotSupportTheDescriptor_whenCheckingWhetherCollectionIsTransactional_thenShouldReturnTrue() {
        doNotSupportTheCurrentDescriptor();

        assertThat(transactivity.isCollectionTransactional(descriptor), is(false));
    }

    private void doNotSupportTheCurrentDescriptor() {
        when(transactor.hostStorageType()).thenReturn(StandardStorageType.MONGO);
    }

    @Test
    void givenTheOnlyTransactorSupportsTheDescriptor_whenDoInTransaction_thenItShouldBeExecuted() {
        supportTheCurrentDescriptor();

        int result = transactivity.doInTransaction(descriptor, () -> 42);
        assertThat(result, is(42));
    }

    @Test
    void givenTheOnlyTransactorDoesNotSupportTheDescriptor_whenDoInTransaction_thenAnExceptionShouldBeThrown() {
        doNotSupportTheCurrentDescriptor();

        try {
            transactivity.doInTransaction(descriptor, () -> 42);
            fail("An exception should be thrown");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("Did not find any transactor for 'postgres'"));
        }
    }

    private static class TestTransactor implements CollectionTransactor {
        @Override
        public StorageType hostStorageType() {
            return StandardStorageType.POSTGRES;
        }

        @Override
        public <T> T doInTransaction(Supplier<T> action) {
            return action.get();
        }
    }
}