package io.extremum.common.descriptor.factory;

import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static io.extremum.sharedmodels.descriptor.StandardStorageType.MONGO;
import static io.extremum.test.mockito.ReturnFirstArg.returnFirstArg;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlankDescriptorSaverTest {
    @Mock
    private DescriptorService descriptorService;

    @Mock
    private DescriptorSavers descriptorSavers;


    @Test
    void whenSavingABatch_thenABatchOfDescriptorsShouldBeSavedAndReturned() {
        when(descriptorService.storeBatch(any())).then(returnFirstArg());
        when(descriptorService.createExternalId()).thenReturn(UUID.randomUUID().toString());

        BlankDescriptorSaver blankDescriptorSaver = new BlankDescriptorSaver(descriptorService);

        List<String> internalIds = Arrays.asList("1", "2", "3");
        List<Descriptor> descriptors = blankDescriptorSaver.createAndSaveBatchOfBlankDescriptors(internalIds, MONGO);

        assertThat(descriptors, hasSize(3));
        assertThat(descriptors, everyItem(hasProperty("storageType", is("mongo"))));
        assertThat(descriptors.get(0).getInternalId(), is("1"));
        assertThat(descriptors.get(1).getInternalId(), is("2"));
        assertThat(descriptors.get(2).getInternalId(), is("3"));

        verify(descriptorService).storeBatch(descriptors);
    }
}