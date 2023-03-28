package io.extremum.common.support;

import io.extremum.sharedmodels.basic.Model;
import io.extremum.common.service.CommonService;

/**
 * @author rpuch
 */
public interface CommonServices {
    <M extends Model> CommonService<M> findServiceByModel(Class<? extends M> modelClass);
}
