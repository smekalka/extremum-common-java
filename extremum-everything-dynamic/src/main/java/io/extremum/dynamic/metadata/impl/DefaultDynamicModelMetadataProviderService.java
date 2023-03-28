package io.extremum.dynamic.metadata.impl;

import io.extremum.dynamic.metadata.MetadataProviderService;
import io.extremum.dynamic.metadata.MetadataSupplier;
import io.extremum.dynamic.models.DynamicModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultDynamicModelMetadataProviderService implements MetadataProviderService {
    private final List<MetadataSupplier> metadataSuppliers;

    @Override
    public <T> void provideMetadata(DynamicModel<T> dynamicModel) {
        metadataSuppliers.stream()
                .filter(u -> u.supports(dynamicModel.getModelName()))
                .forEach(u -> u.process(dynamicModel));
    }
}
