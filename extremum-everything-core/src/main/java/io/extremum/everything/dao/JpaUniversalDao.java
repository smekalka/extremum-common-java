package io.extremum.everything.dao;

import io.extremum.everything.collection.CollectionFragment;
import io.extremum.everything.collection.Projection;
import org.apache.commons.lang3.NotImplementedException;
import reactor.core.publisher.Flux;

import java.util.List;

public class JpaUniversalDao implements UniversalDao {
    @Override
    public <T> CollectionFragment<T> retrieveByIds(List<?> ids, Class<T> classOfElement, Projection projection) {
        //TODO: Implement here
        throw new NotImplementedException();
    }

    @Override
    public <T> Flux<T> streamByIds(List<?> ids, Class<T> classOfElement, Projection projection) {
        //TODO: Implement here
        throw new NotImplementedException();
    }
}
