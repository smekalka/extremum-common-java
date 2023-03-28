package io.extremum.everything.services.defaultservices;

import io.extremum.sharedmodels.basic.Model;
import io.extremum.common.service.CommonService;
import io.extremum.common.support.CommonServices;
import io.extremum.everything.support.ModelDescriptors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultRemoverImpl implements DefaultRemover {
    private final CommonServices commonServices;
    private final ModelDescriptors modelDescriptors;

    @Override
    public void remove(String id) {
        Class<? extends Model> modelClass = modelDescriptors.getModelClassByModelInternalId(id);
        CommonService<? extends Model> service = commonServices.findServiceByModel(modelClass);
        service.delete(id);
    }

}
