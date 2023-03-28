package io.extremum.security;

import io.extremum.security.services.DataAccessChecker;
import io.extremum.sharedmodels.basic.Model;

abstract class SamePolicyChecker<M extends Model> implements DataAccessChecker<M> {
    @Override
    public final boolean allowedToGet(M model, CheckerContext context) {
        return allowed(model, context);
    }

    @Override
    public final boolean allowedToPatch(M model, CheckerContext context) {
        return allowed(model, context);
    }

    @Override
    public final boolean allowedToRemove(M model, CheckerContext context) {
        return allowed(model, context);
    }

    @Override
    public boolean allowedToWatch(M model, CheckerContext context) {
        return allowed(model, context);
    }

    @Override
    public boolean allowedToCreate(M model, CheckerContext context) {
        return allowed(model, context);
    }

    abstract boolean allowed(M model, CheckerContext context);
}
