package io.extremum.everything.services.defaultservices;

import io.extremum.common.model.CollectionFilter;
import io.extremum.common.service.CommonService;
import io.extremum.common.support.CommonServices;
import io.extremum.common.support.ModelClasses;
import io.extremum.everything.support.ModelDescriptors;
import io.extremum.sharedmodels.basic.Model;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class DefaultGetterViaCommonServices implements DefaultGetter {
    private final CommonServices commonServices;
    private final ModelDescriptors modelDescriptors;
    private final ModelClasses modelClasses;

    @Override
    public Model get(String internalId) {
        CommonService<Model> service = findService(internalId);
        return service.get(internalId);
    }

    private CommonService<Model> findService(String internalId) {
        Class<Model> modelClass = modelDescriptors.getModelClassByModelInternalId(internalId);
        return commonServices.findServiceByModel(modelClass);
    }

    protected CommonService<Model> findServiceByModelName(String modelName) {
        Class<Model> modelClass = modelClasses.getClassByModelName(modelName);
        return commonServices.findServiceByModel(modelClass);
    }

    @Override
    public Page<Model> getAll(String modelName, Pageable pageable) {
        CommonService<Model> service = findServiceByModelName(modelName);
        return service.findAll(pageable);
    }

    @Override
    public List<Model> getAllByIds(List<String> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        CommonService<Model> service = findService(ids.get(0));
        return service.findAll(ids);
    }

    @Override
    public Page<Model> getAll(String modelName, CollectionFilter filter, Pageable pageable) {
        CommonService<Model> service = findServiceByModelName(modelName);
        return service.findAll(pageable);
    }
}
