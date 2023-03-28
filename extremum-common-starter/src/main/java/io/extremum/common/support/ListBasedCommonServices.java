package io.extremum.common.support;

import io.extremum.sharedmodels.basic.Model;
import io.extremum.common.service.CommonService;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author rpuch
 */
public final class ListBasedCommonServices implements CommonServices {
    private final Map<Class<? extends Model>, CommonService<? extends Model>> modelClassToServiceMap;

    public ListBasedCommonServices(List<CommonService<? extends Model>> services) {
        Map<Class<? extends Model>, CommonService<? extends Model>> map = new HashMap<>();
        for (CommonService<? extends Model> service : services)  {
            Class<? extends Model> modelClass = CommonServiceUtils.findCommonServiceModelClass(service);
            map.put(modelClass, service);
        }

        modelClassToServiceMap = ImmutableMap.copyOf(map);
    }

    public <M extends Model> CommonService<M> findServiceByModel(Class<? extends M> modelClass) {
        @SuppressWarnings("unchecked")
        CommonService<M> service = (CommonService<M>) modelClassToServiceMap.get(modelClass);
        if (service == null) {
            throw new RuntimeException("Cannot find implementation of CommonService for model " + modelClass);
        }
        return service;
    }
}
