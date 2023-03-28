package io.extremum.watch.services;

import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.common.utils.FindUtils;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.signal.SignalEmitter;
import io.extremum.sharedmodels.watch.ModelSignalType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ScanningModelSignalRegistrar implements ModelSignalRegistrar {

    private final Map<Class<? extends Model>, List<ModelSignalType>> map = new HashMap<>();

    public ScanningModelSignalRegistrar(List<String> packageNames) {
        packageNames
                .stream().map(
                        pckge -> FindUtils.findClassesByAnnotation(BasicModel.class, SignalEmitter.class, pckge)
                )
                .forEach(
                        classes -> classes.forEach(
                                signalEmitterClass -> {
                                    Arrays.stream(signalEmitterClass.getAnnotation(SignalEmitter.class).value()).forEach(
                                            modelSignalType -> register(signalEmitterClass, modelSignalType)
                                    );
                                }
                        )
                );
    }

    @Override
    public void register(Class<? extends Model> signalEmitterClass, ModelSignalType signalType) {
        List<ModelSignalType> signalTypes = map.computeIfAbsent(signalEmitterClass, key -> new ArrayList<>());
        signalTypes.add(signalType);
    }


    @Override
    public boolean isRegistered(Class<? extends Model> modelClass, ModelSignalType signalType) {
        List<ModelSignalType> signalTypes = map.get(modelClass);
        return signalTypes != null && !signalTypes.isEmpty() && signalTypes.contains(signalType);
    }
}
