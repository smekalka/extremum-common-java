package io.extremum.everything.services.defaultservices;

import io.extremum.sharedmodels.basic.Model;
import io.extremum.common.service.CommonService;
import io.extremum.common.support.CommonServices;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultSaverImpl implements DefaultSaver {
    private final CommonServices commonServices;

    @Override
    public Model save(Model model) {
        CommonService<Model> service = commonServices.findServiceByModel(model.getClass());
        return service.save(model);
    }
}
