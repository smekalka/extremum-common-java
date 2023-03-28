package io.extremum.everything.services.management;

import io.extremum.common.modelservices.ModelServices;
import io.extremum.common.utils.ModelUtils;
import io.extremum.everything.services.ReactiveSaverService;
import io.extremum.everything.services.defaultservices.DefaultReactiveSaver;
import io.extremum.sharedmodels.basic.Model;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author rpuch
 */
@Slf4j
@RequiredArgsConstructor
public class ReactiveModelSaver {
    private final List<ReactiveSaverService<?>> saverServices;
    private final DefaultReactiveSaver defaultSaver;

    public Mono<Model> saveModel(Model model) {
        return Mono.just(model)
                .map(ModelUtils::getModelName)
                .map(this::findSaver)
                .flatMap(saver -> {
                    return saver.save(model);
                });
    }

    private ReactiveSaver findSaver(String modelName) {
        ReactiveSaverService<? extends Model> service = ModelServices.findServiceForModel(modelName, saverServices);
        if (service != null) {
            return new NonDefaultReactiveSaver(service);
        }

        return defaultSaver;
    }
}
