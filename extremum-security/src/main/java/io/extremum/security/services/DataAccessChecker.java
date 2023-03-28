package io.extremum.security.services;

import io.extremum.common.modelservices.ModelService;
import io.extremum.security.CheckerContext;
import io.extremum.sharedmodels.basic.Model;

public interface DataAccessChecker<M extends Model> extends ModelService {
    boolean allowedToGet(M model, CheckerContext context);

    boolean allowedToPatch(M model, CheckerContext context);

    boolean allowedToRemove(M model, CheckerContext context);

    boolean allowedToWatch(M model, CheckerContext context);

    boolean allowedToCreate(M model, CheckerContext context);
}