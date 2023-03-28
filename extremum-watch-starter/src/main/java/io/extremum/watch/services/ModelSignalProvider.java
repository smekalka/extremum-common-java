package io.extremum.watch.services;

import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.watch.ModelSignal;
import io.extremum.sharedmodels.watch.ModelSignalType;
import org.reactivestreams.Publisher;

import java.security.Principal;

public interface ModelSignalProvider {

    <M extends Model> Publisher<M> subscribe(String modelId, ModelSignalType type);

    <M extends Model> Publisher<M> subscribe(Class<? extends Model> modelClass, ModelSignalType type);

    void publish(ModelSignal signal) throws InterruptedException;
}
