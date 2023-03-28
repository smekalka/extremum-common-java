package io.extremum.mongo.springdata.repository;

import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.common.model.PersistableCommonModel;
import io.extremum.mongo.SoftDeletion;
import io.extremum.mongo.dao.MongoCommonDao;
import io.extremum.mongo.model.MongoCommonModel;
import org.apache.commons.lang3.NotImplementedException;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.data.util.StreamUtils;
import org.springframework.data.util.Streamable;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * Differs from the standard {@link SimpleMongoRepository} in two aspects:
 * 1. has implementations for our extension methods
 * 2. implements soft-deletion logic; that is, all deletions are replaced with setting 'deleted' flag to true,
 * and all find operations filter out documents with 'deleted' set to true.
 *
 * @author rpuch
 */
public class SoftDeleteMongoRepository<T extends MongoCommonModel> extends BaseMongoRepository<T>
        implements MongoCommonDao<T> {
    private static final String ID = "_id";
    private static final String DELETED = PersistableCommonModel.FIELDS.deleted.name();

    private final MongoEntityInformation<T, ObjectId> metadata;
    private final MongoOperations mongoOperations;
    private final SoftDeletion softDeletion = new SoftDeletion();

    public SoftDeleteMongoRepository(MongoEntityInformation<T, ObjectId> metadata,
                                     MongoOperations mongoOperations) {
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
    public Optional<T> findById(ObjectId id) {
        Assert.notNull(id, "The given id must not be null!");

        Query query = notDeletedQueryWith(getIdCriteria(id));

        return findOneByQuery(query);
    }

    @Override
    public Iterable<T> findAllById(Iterable<ObjectId> ids) {
        Criteria inCriteria = new Criteria(metadata.getIdAttribute())
                .in(Streamable.of(ids).stream().collect(StreamUtils.toUnmodifiableList()));
        return findAllByQuery(notDeletedQueryWith(inCriteria));
    }

    @Override
    public long count() {
        return mongoOperations.count(notDeletedQuery(), metadata.getCollectionName());
    }

    @Override
    public <S extends T> long count(Example<S> example) {
        Assert.notNull(example, "Sample must not be null!");

        Query q = notDeletedQueryWith(new Criteria().alike(example));
        return mongoOperations.count(q, example.getProbeType(), metadata.getCollectionName());
    }

    private Criteria notDeleted() {
        return softDeletion.notDeleted();
    }

    @Override
    public List<T> findAll() {
        return findAllByQuery(notDeletedQuery());
    }

    @Override
    public List<T> findAll(Sort sort) {
        return findAllByQuery(notDeletedQuery().with(sort));
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        List<T> list = findAllByQuery(notDeletedQuery().with(pageable));
        long count = count();
        return new PageImpl<>(list, pageable, count);
    }

    @Override
    public <S extends T> List<S> findAll(Example<S> example, Sort sort) {
        Assert.notNull(example, "Sample must not be null!");
        Assert.notNull(sort, "Sort must not be null!");

        Query q = queryForNotDeletedAndAlike(example).with(sort);

        return mongoOperations.find(q, example.getProbeType(), metadata.getCollectionName());
    }

    @Override
    public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
        Assert.notNull(example, "Sample must not be null!");
        Assert.notNull(pageable, "Pageable must not be null!");

        Query q = queryForNotDeletedAndAlike(example).with(pageable);
        List<S> list = mongoOperations.find(q, example.getProbeType(), metadata.getCollectionName());

        return PageableExecutionUtils.getPage(list, pageable,
                () -> mongoOperations.count(q, example.getProbeType(), metadata.getCollectionName()));
    }

    @Override
    public <S extends T> Optional<S> findOne(Example<S> example) {
        Assert.notNull(example, "Sample must not be null!");

        Query q = queryForNotDeletedAndAlike(example);
        return Optional
                .ofNullable(mongoOperations.findOne(q, example.getProbeType(), metadata.getCollectionName()));
    }

    @Override
    public <S extends T> boolean exists(Example<S> example) {
        Assert.notNull(example, "Sample must not be null!");

        Query q = queryForNotDeletedAndAlike(example);
        return mongoOperations.exists(q, example.getProbeType(), metadata.getCollectionName());
    }

    private <S extends T> Query queryForNotDeletedAndAlike(Example<S> example) {
        return notDeletedQueryWith(new Criteria().alike(example));
    }

    @Override
    public boolean existsById(ObjectId id) {
        Assert.notNull(id, "The given id must not be null!");

        return mongoOperations.exists(notDeletedQueryWith(where(metadata.getIdAttribute()).is(id)),
                metadata.getJavaType(), metadata.getCollectionName());
    }

    @Override
    public void deleteById(ObjectId id) {
        deleteByIdAndReturn(id);
    }

    private Update updateDeletedToTrue() {
        Update update = new Update();
        update.set(DELETED, true);
        return update;
    }

    @Override
    public T deleteByIdAndReturn(ObjectId id) {
        Query query = new Query(where(ID).is(id));
        Update update = updateDeletedToTrue();
        T deletedModel = mongoOperations.findAndModify(query, update, metadata.getJavaType());
        if (deletedModel == null) {
            throw new ModelNotFoundException(metadata.getJavaType(), id.toString());
        }
        return deletedModel;
    }

    @Override
    public void delete(T entity) {
        entity.setDeleted(true);
        save(entity);
    }

    @Override
    public void deleteAll(Iterable<? extends T> entities) {
        Assert.notNull(entities, "The given Iterable of entities must not be null!");

        entities.forEach(this::delete);
    }

    @Override
    public Page<T> findAll(Specification<T> specification, Pageable pageable) {
        throw new NotImplementedException();
    }
}
