package io.extremum.everything.services.management;

import io.extremum.sharedmodels.basic.Model;
import io.extremum.everything.services.SaverService;

/**
 * Uses SaverService to save an entity.
 *
 * @author rpuch
 */
final class NonDefaultSaver implements Saver {
    private final SaverService<Model> saverService;

    NonDefaultSaver(SaverService<? extends Model> saverService) {
        this.saverService = (SaverService<Model>) saverService;
    }

    @Override
    public Model save(Model model) {
        return saverService.save(model);
    }
}
