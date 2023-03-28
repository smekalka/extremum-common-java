package io.extremum.everything.services.management;

import io.extremum.common.modelservices.ModelServices;
import io.extremum.common.utils.ModelUtils;
import io.extremum.everything.services.SaverService;
import io.extremum.everything.services.defaultservices.DefaultSaver;
import io.extremum.sharedmodels.basic.Model;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author rpuch
 */
@Slf4j
@RequiredArgsConstructor
public class ModelSaver {
    private final List<SaverService<?>> saverServices;
    private final DefaultSaver defaultSaver;

    public Model saveModel(Model model) {
        String modelName = ModelUtils.getModelName(model);
        Saver saver = findSaver(modelName);
        return saver.save(model);
    }

    private Saver findSaver(String modelName) {
        SaverService<? extends Model> service = ModelServices.findServiceForModel(modelName, saverServices);
        if (service != null) {
            return new NonDefaultSaver(service);
        }

        return defaultSaver;
    }
}
