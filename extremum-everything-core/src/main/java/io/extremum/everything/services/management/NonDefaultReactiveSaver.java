package io.extremum.everything.services.management;

import io.extremum.everything.services.ReactiveSaverService;
import io.extremum.sharedmodels.basic.Model;
import reactor.core.publisher.Mono;

/**
 * Uses ReactiveSaverService to save an entity.
 *
 * @author rpuch
 */
final class NonDefaultReactiveSaver implements ReactiveSaver {
    private final ReactiveSaverService<Model> saverService;

    NonDefaultReactiveSaver(ReactiveSaverService<? extends Model> saverService) {
        this.saverService = (ReactiveSaverService<Model>) saverService;
    }

    @Override
    public Mono<Model> save(Model model) {
        return saverService.save(model);
    }
}
