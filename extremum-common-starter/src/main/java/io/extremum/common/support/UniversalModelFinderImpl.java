package io.extremum.common.support;

import io.extremum.sharedmodels.basic.Model;
import io.extremum.common.service.CommonService;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author rpuch
 */
@RequiredArgsConstructor
public class UniversalModelFinderImpl implements UniversalModelFinder {
    private final ModelClasses modelClasses;
    private final CommonServices commonServices;

    @Override
    public List<Model> findModels(List<Descriptor> descriptors) {
        return descriptors.stream()
                .map(this::loadModelByDescriptor)
                .collect(Collectors.toList());
    }

    private Model loadModelByDescriptor(Descriptor descriptor) {
        Class<Model> modelClass = modelClasses.getClassByModelName(descriptor.getModelType());
        CommonService<Model> service = commonServices.findServiceByModel(modelClass);
        return service.get(descriptor.getInternalId());
    }
}
