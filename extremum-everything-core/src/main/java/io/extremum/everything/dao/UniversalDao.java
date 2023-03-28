package io.extremum.everything.dao;

import io.extremum.everything.collection.CollectionFragment;
import io.extremum.everything.collection.Projection;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * @author rpuch
 */
public interface UniversalDao {
    <T> CollectionFragment<T> retrieveByIds(List<?> ids, Class<T> classOfElement, Projection projection);

    <T> Flux<T> streamByIds(List<?> ids, Class<T> classOfElement, Projection projection);
}
