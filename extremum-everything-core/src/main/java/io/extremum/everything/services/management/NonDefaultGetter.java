package io.extremum.everything.services.management;

import io.extremum.common.model.CollectionFilter;
import io.extremum.everything.services.GetterService;
import io.extremum.sharedmodels.basic.Model;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

final class NonDefaultGetter implements Getter {
    private final GetterService<Model> getterService;

    NonDefaultGetter(GetterService<Model> getterService) {
        this.getterService = getterService;
    }

    @Override
    public Model get(String id) {
        return getterService.get(id);
    }

    @Override
    public Page<Model> getAll(String modelName, Pageable pageable) {
        return getterService.getAll(pageable);
    }

    @Override
    public Page<Model> getAll(String modelName, CollectionFilter filter, Pageable pageable) {
        throw new NotImplementedException();
    }

    @Override
    public List<Model> getAllByIds(List<String> ids) {
        return getterService.getAllByIds(ids);
    }
}
