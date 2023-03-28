package io.extremum.everything.services.collection;

import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.everything.collection.CollectionFragment;
import io.extremum.everything.collection.Projection;
import io.extremum.everything.dao.UniversalDao;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.function.Function;

/**
 * @author rpuch
 */
public final class StreamByOwnedCoordinates extends GetByOwnedCoordinates<Flux<Model>> {
    private final UniversalDao universalDao;

    public StreamByOwnedCoordinates(UniversalDao universalDao) {
        this.universalDao = universalDao;
    }

    public final Flux<Model> stream(BasicModel host, String hostAttributeName, Projection projection) {
        return getCollection(host, hostAttributeName, projection);
    }

    @Override
    Flux<Model> emptyResult() {
        return Flux.empty();
    }

    @Override
    Flux<Model> retrieveModelsByIds(List<?> ids, Class<? extends Model> classOfElement,
                                    Projection projection) {
        return universalDao.streamByIds(ids, classOfElement, projection)
                .map(Function.identity());
    }

    @Override
    Flux<Model> collectionFragmentToResult(CollectionFragment<Model> fragment) {
        return Flux.fromIterable(fragment.elements());
    }

}
