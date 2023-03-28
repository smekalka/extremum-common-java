package io.extremum.common.descriptorpool;

import io.extremum.common.descriptor.factory.BlankDescriptorSaver;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.Descriptor.Readiness;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static io.extremum.sharedmodels.descriptor.StandardStorageType.MONGO;
import static io.extremum.test.mockito.ReturnFirstArg.returnFirstArg;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DescriptorAllocatorTest {
    @Mock
    private DescriptorService descriptorService;
    private final InternalIdGenerator idGenerator = new MongoIdGenerator();

    @Test
    void whenAllocating_thenDescriptorsShouldBeStoredInABatchViaDescriptorService() {
        when(descriptorService.storeBatch(anyListOfSize(10)))
                .then(returnFirstArg());
        when(descriptorService.createExternalId()).thenReturn(UUID.randomUUID().toString());

        DescriptorAllocator allocator = new DescriptorAllocator(
                new BlankDescriptorSaver(descriptorService), MONGO, idGenerator);

        List<Descriptor> allocated = allocator.allocate(10);

        assertThat(allocated, hasSize(10));
        assertThat(allocated, everyItem(hasProperty("internalId", notNullValue())));
        assertThat(allocated, everyItem(hasProperty("readiness", is(Readiness.BLANK))));
    }

    private List<Descriptor> anyListOfSize(int expectedSize) {
        return argThat(new AnyListOfSizeMatcher<>(expectedSize));
    }

    private static class MongoIdGenerator implements InternalIdGenerator {
        @Override
        public String generateInternalId() {
            return new ObjectId().toString();
        }
    }

    @RequiredArgsConstructor
    private static class AnyListOfSizeMatcher<T> implements ArgumentMatcher<List<T>> {
        private final int expectedSize;

        @Override
        public boolean matches(List<T> list) {
            return list != null && list.size() == expectedSize;
        }
    }
}