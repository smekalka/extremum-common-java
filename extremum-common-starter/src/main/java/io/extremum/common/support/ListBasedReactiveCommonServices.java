package io.extremum.common.support;

import com.google.common.collect.ImmutableMap;
import io.extremum.common.service.ReactiveCommonService;
import io.extremum.sharedmodels.basic.Model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author rpuch
 */
public final class ListBasedReactiveCommonServices implements ReactiveCommonServices {
    private final Map<Class<? extends Model>, ReactiveCommonService<? extends Model>> modelClassToServiceMap;

    public ListBasedReactiveCommonServices(List<ReactiveCommonService<? extends Model>> services) {
        Map<Class<? extends Model>, ReactiveCommonService<? extends Model>> map = new HashMap<>();
        for (ReactiveCommonService<? extends Model> service : services)  {
            Class<? extends Model> modelClass = CommonServiceUtils.findReactiveCommonServiceModelClass(service);
            map.put(modelClass, service);
        }

        modelClassToServiceMap = ImmutableMap.copyOf(map);
    }

    public <M extends Model> ReactiveCommonService<M> findServiceByModel(Class<? extends M> modelClass) {
        @SuppressWarnings("unchecked")
        ReactiveCommonService<M> service = (ReactiveCommonService<M>) modelClassToServiceMap.get(modelClass);
        if (service == null) {
            throw new RuntimeException("Cannot find implementation of ReactiveCommonService for model " + modelClass);
        }
        return service;
    }
}
