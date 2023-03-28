package io.extremum.common.support;

import io.extremum.common.service.ReactiveCommonService;
import io.extremum.sharedmodels.basic.Model;

/**
 * @author rpuch
 */
public interface ReactiveCommonServices {
    <M extends Model> ReactiveCommonService<M> findServiceByModel(Class<? extends M> modelClass);
}
