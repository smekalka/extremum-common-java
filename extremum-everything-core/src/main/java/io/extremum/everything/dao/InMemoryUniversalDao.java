package io.extremum.everything.dao;

import io.extremum.everything.collection.CollectionFragment;
import io.extremum.everything.collection.Projection;
import reactor.core.publisher.Flux;

import java.util.List;

public class InMemoryUniversalDao implements UniversalDao {
    @Override
    public <T> CollectionFragment<T> retrieveByIds(List<?> ids, Class<T> classOfElement, Projection projection) {
        return null;
    }

    @Override
    public <T> Flux<T> streamByIds(List<?> ids, Class<T> classOfElement, Projection projection) {
        return null;
    }
}
