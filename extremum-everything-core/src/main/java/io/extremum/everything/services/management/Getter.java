package io.extremum.everything.services.management;

import io.extremum.common.model.CollectionFilter;
import io.extremum.sharedmodels.basic.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface Getter {
    Model get(String id);

    Page<Model> getAll(String modelName, Pageable pageable);

    Page<Model> getAll(String modelName, CollectionFilter filter, Pageable pageable);

    List<Model> getAllByIds(List<String> ids);
}
