package io.extremum.everything.dao;

import io.extremum.common.model.PersistableCommonModel;
import io.extremum.mongo.SoftDeletion;
import io.extremum.everything.collection.CollectionFragment;
import io.extremum.everything.collection.Projection;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * @author rpuch
 */
@Repository
public class MongoUniversalDao implements UniversalDao {
    private static final String CREATED = PersistableCommonModel.FIELDS.created.name();

    private final MongoOperations mongoOperations;
    private final ReactiveMongoOperations reactiveMongoOperations;
    private final SoftDeletion softDeletion = new SoftDeletion();

    public MongoUniversalDao(MongoOperations mongoOperations, ReactiveMongoOperations reactiveMongoOperations) {
        this.mongoOperations = mongoOperations;
        this.reactiveMongoOperations = reactiveMongoOperations;
    }

    @Override
    public <T> CollectionFragment<T> retrieveByIds(List<?> ids, Class<T> classOfElement, Projection projection) {
        List<T> elements = mongoOperations.find(
                criteriaToSearchByIdsWithSortAndPaging(ids, projection), classOfElement);
        long count = mongoOperations.count(criteriaToSearchByIds(ids, projection), classOfElement);

        return CollectionFragment.forFragment(elements, count);
    }

    private Query criteriaToSearchByIdsWithSortAndPaging(List<?> ids, Projection projection) {
        Query query = criteriaToSearchByIds(ids, projection);

        projection.getLimit().ifPresent(query::limit);
        projection.getOffset().ifPresent(query::skip);

        query.with(Sort.by(
                Order.by(CREATED),
                Order.by(PersistableCommonModel.FIELDS.id.name())
        ));

        return query;
    }

    private Query criteriaToSearchByIds(List<?> ids, Projection projection) {
        List<Criteria> criteria = new ArrayList<>();

        criteria.add(where(PersistableCommonModel.FIELDS.id.name()).in(ids));
        criteria.add(softDeletion.notDeleted());

        projection.getSince().ifPresent(since -> criteria.add(where(CREATED).gte(since)));
        projection.getUntil().ifPresent(until -> criteria.add(where(CREATED).lt(until)));

        return new Query(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
    }

    @Override
    public <T> Flux<T> streamByIds(List<?> ids, Class<T> classOfElement, Projection projection) {
        Query query = criteriaToSearchByIdsWithSortAndPaging(ids, projection);
        return reactiveMongoOperations.find(query, classOfElement);
    }
}
