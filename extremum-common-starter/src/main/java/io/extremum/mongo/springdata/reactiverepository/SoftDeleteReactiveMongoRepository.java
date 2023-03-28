package io.extremum.mongo.springdata.reactiverepository;

import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.common.model.PersistableCommonModel;
import io.extremum.mongo.SoftDeletion;
import io.extremum.mongo.dao.ReactiveMongoCommonDao;
import io.extremum.mongo.model.MongoCommonModel;
import org.bson.types.ObjectId;
import org.reactivestreams.Publisher;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleReactiveMongoRepository;
import org.springframework.data.util.StreamUtils;
import org.springframework.data.util.Streamable;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * Differs from the standard {@link SimpleReactiveMongoRepository} in two aspects:
 * 1. has implementations for our extension methods
 * 2. implements soft-deletion logic; that is, all deletions are replaced with setting 'deleted' flag to true,
 * and all find operations filter out documents with 'deleted' set to true.
 *
 * @author rpuch
 */
public class SoftDeleteReactiveMongoRepository<T extends MongoCommonModel> extends BaseReactiveMongoRepository<T>
        implements ReactiveMongoCommonDao<T> {
    private static final String ID = "_id";
    private static final String DELETED = PersistableCommonModel.FIELDS.deleted.name();

    private final MongoEntityInformation<T, ObjectId> metadata;
    private final ReactiveMongoOperations mongoOperations;
    private final SoftDeletion softDeletion = new SoftDeletion();

    public SoftDeleteReactiveMongoRepository(MongoEntityInformation<T, ObjectId> metadata,
                                             ReactiveMongoOperations mongoOperations) {
        super(metadata, mongoOperations);

        this.metadata = metadata;
        this.mongoOperations = mongoOperations;
    }

    @Override
    Query notDeletedQueryWith(Criteria criteria) {
        Criteria finalCriteria = new Criteria().andOperator(
                notDeleted(),
                criteria
        );
        return new Query(finalCriteria);
    }

    @Override
    Query notDeletedQuery() {
        return new Query(notDeleted());
    }

    @Override
    public Mono<T> findById(ObjectId id) {
        Assert.notNull(id, "The given id must not be null!");

        Query query = notDeletedQueryWith(getIdCriteria(id));

        return findOneByQuery(query);
    }

    @Override
    public Mono<T> findById(Publisher<ObjectId> publisher) {
        Assert.notNull(publisher, "The given id must not be null!");

        return Mono.from(publisher).flatMap(this::findById);
    }

    @Override
    public Flux<T> findAllById(Iterable<ObjectId> ids) {
        Criteria inCriteria = new Criteria(metadata.getIdAttribute())
                .in(Streamable.of(ids).stream().collect(StreamUtils.toUnmodifiableList()));
        return findAllByQuery(notDeletedQueryWith(inCriteria));
    }

    @Override
    public Mono<Long> count() {
        return mongoOperations.count(notDeletedQuery(), metadata.getCollectionName());
    }

    @Override
    public <S extends T> Mono<Long> count(Example<S> example) {
        Assert.notNull(example, "Sample must not be null!");

        Query q = notDeletedQueryWith(new Criteria().alike(example));
        return mongoOperations.count(q, example.getProbeType(), metadata.getCollectionName());
    }

    private Criteria notDeleted() {
        return softDeletion.notDeleted();
    }

    @Override
    public Flux<T> findAll() {
        return findAllByQuery(notDeletedQuery());
    }

    @Override
    public Flux<T> findAll(Sort sort) {
        return findAllByQuery(notDeletedQuery().with(sort));
    }

    @Override
    public <S extends T> Flux<S> findAll(Example<S> example, Sort sort) {
        Assert.notNull(example, "Sample must not be null!");
        Assert.notNull(sort, "Sort must not be null!");

        Query q = queryForNotDeletedAndAlike(example).with(sort);

        return mongoOperations.find(q, example.getProbeType(), metadata.getCollectionName());
    }

    @Override
    public <S extends T> Mono<S> findOne(Example<S> example) {
        Assert.notNull(example, "Sample must not be null!");

        Query q = queryForNotDeletedAndAlike(example);
        return mongoOperations.findOne(q, example.getProbeType(), metadata.getCollectionName());
    }

    @Override
    public <S extends T> Mono<Boolean> exists(Example<S> example) {
        Assert.notNull(example, "Sample must not be null!");

        Query q = queryForNotDeletedAndAlike(example);
        return mongoOperations.exists(q, example.getProbeType(), metadata.getCollectionName());
    }

    private <S extends T> Query queryForNotDeletedAndAlike(Example<S> example) {
        return notDeletedQueryWith(new Criteria().alike(example));
    }

    @Override
    public Mono<Boolean> existsById(ObjectId id) {
        Assert.notNull(id, "The given id must not be null!");

        return mongoOperations.exists(notDeletedQueryWith(where(metadata.getIdAttribute()).is(id)),
                metadata.getJavaType(), metadata.getCollectionName());
    }

    @Override
    public Mono<Boolean> existsById(Publisher<ObjectId> publisher) {
        Assert.notNull(publisher, "The given id must not be null!");

        return Mono.from(publisher).flatMap(this::existsById);
    }

    @Override
    public Mono<Void> deleteById(ObjectId id) {
        return deleteByIdAndReturn(id).then();
    }

    @Override
    public Mono<Void> deleteById(Publisher<ObjectId> publisher) {
        Assert.notNull(publisher, "Id must not be null!");

        return Mono.from(publisher).flatMap(this::deleteById);
    }

    private Update updateDeletedToTrue() {
        Update update = new Update();
        update.set(DELETED, true);
        return update;
    }

    @Override
    public Mono<T> deleteByIdAndReturn(ObjectId id) {
        Query query = new Query(where(ID).is(id));
        Update update = updateDeletedToTrue();
        return mongoOperations.findAndModify(query, update, metadata.getJavaType())
                .switchIfEmpty(Mono.error(new ModelNotFoundException(metadata.getJavaType(), id.toString())));
    }

    @Override
    public Mono<Void> delete(T entity) {
        entity.setDeleted(true);
        return save(entity).then();
    }
}
