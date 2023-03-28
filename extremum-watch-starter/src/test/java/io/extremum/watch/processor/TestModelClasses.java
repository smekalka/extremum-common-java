package io.extremum.watch.processor;

import io.extremum.sharedmodels.basic.Model;
import io.extremum.common.support.ModelClasses;

/**
 * @author rpuch
 */
class TestModelClasses implements ModelClasses {
    @SuppressWarnings("unchecked")
    @Override
    public <M extends Model> Class<M> getClassByModelName(String modelName) {
        if (ProcessorTests.WATCHED_MODEL_NAME.equals(modelName)) {
            return (Class<M>) WatchedModel.class;
        }
        if (ProcessorTests.NON_WATCHED_MODEL_NAME.equals(modelName)) {
            return (Class<M>) NonWatchedModel.class;
        }
        throw new IllegalStateException(String.format("We don't know '%s'", modelName));
    }

}
