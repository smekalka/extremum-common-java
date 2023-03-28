package io.extremum.dynamic.metadata.impl;

import io.extremum.dynamic.metadata.MetadataSupplier;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

class DefaultDynamicModelMetadataSupplierServiceTest {
    @Test
    void provideMetadata() {
        MetadataSupplier validSupplier = createValidSupplier();
        MetadataSupplier notValidSupplier = createNotValidSupplier();

        List<MetadataSupplier> providers = new ArrayList<>();
        providers.add(validSupplier);
        providers.add(notValidSupplier);

        DefaultDynamicModelMetadataProviderService providerService = new DefaultDynamicModelMetadataProviderService(providers);

        JsonDynamicModel model = new JsonDynamicModel("TestedModel", null);

        providerService.provideMetadata(model);

        verify(validSupplier, times(1)).supports(eq(model.getModelName()));
        verify(notValidSupplier, times(1)).supports(eq(model.getModelName()));

        verify(validSupplier, times(1)).process(eq(model));
        verify(notValidSupplier, never()).process(any());
    }

    private MetadataSupplier createValidSupplier() {
        return createSupplier(true);
    }

    private MetadataSupplier createNotValidSupplier() {
        return createSupplier(false);
    }

    private MetadataSupplier createSupplier(boolean supports) {
        MetadataSupplier supplier = mock(MetadataSupplier.class);

        doReturn(supports).when(supplier).supports(any());

        return supplier;
    }
}
