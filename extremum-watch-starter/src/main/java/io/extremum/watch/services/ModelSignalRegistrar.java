package io.extremum.watch.services;

import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.watch.ModelSignalType;

public interface ModelSignalRegistrar {

    void register(Class<? extends Model> signalEmitterClass, ModelSignalType signalType);

    boolean isRegistered(Class<? extends Model> modelClass, ModelSignalType signalType);

}