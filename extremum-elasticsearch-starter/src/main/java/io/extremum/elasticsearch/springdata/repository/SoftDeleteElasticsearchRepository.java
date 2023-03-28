package io.extremum.elasticsearch.springdata.repository;

import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.common.model.PersistableCommonModel;
import io.extremum.common.utils.StreamUtils;
import io.extremum.elasticsearch.SoftDeletion;
import io.extremum.elasticsearch.model.ElasticsearchCommonModel;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchEntityInformation;
import org.springframework.data.elasticsearch.repository.support.SimpleElasticsearchRepository;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.*;

/**
 * Differs from the standard {@link SimpleElasticsearchRepository} in two aspects:
 * 1. has implementations for our extension methods
 * 2. implements soft-deletion logic; that is, all deletions are replaced with setting 'deleted' flag to true,
 * and all find operations filter out documents with 'deleted' set to true.
 *
 * @author rpuch
 */
public class SoftDeleteElasticsearchRepository<T extends ElasticsearchCommonModel>
        extends BaseElasticsearchRepository<T> {

    private final SoftDeletion softDeletion = new SoftDeletion();

    public SoftDeleteElasticsearchRepository(
            ElasticsearchEntityInformation<T, String> metadata,
            ElasticsearchOperations elasticsearchOperations) {
        super(metadata, elasticsearchOperations);
    }

    @Override
    public Iterable<T> search(QueryBuilder query) {
        QueryBuilder amendedQueryBuilder = softDeletion.amendQueryBuilderWithNotDeletedCondition(query);
        return super.search(amendedQueryBuilder);
    }

    @Override
    public Page<T> search(QueryBuilder query, Pageable pageable) {
        QueryBuilder amendedQueryBuilder = softDeletion.amendQueryBuilderWithNotDeletedCondition(query);
        return super.search(amendedQueryBuilder, pageable);
    }

    @Override
    public Page<T> search(Query query) {
        // TODO: can we do it better to filter on the server side?
        Page<T> page = super.search(query);
        return pageOfNonDeleted(page);
    }

    private Page<T> pageOfNonDeleted(Page<T> page) {
        List<T> list = page.stream()
                .filter(PersistableCommonModel::isNotDeleted)
                .collect(toList());
        return new PageImpl<>(list, page.getPageable(), page.getTotalElements());
    }

    @Override
    public Page<T> searchSimilar(T entity, @Nullable String[] fields, Pageable pageable) {
        // TODO: can we do it better to filter on the server side?
        Page<T> page = super.searchSimilar(entity, fields, pageable);
        return pageOfNonDeleted(page);
    }

    @Override
    public void deleteById(String id) {
        patch(id, "ctx._source.deleted = true");
    }

    @Override
    public T deleteByIdAndReturn(String id) {
        T model = findById(id).orElseThrow(() -> new ModelNotFoundException(entityClass, id));

        deleteById(id);

        // I did not find any way to do it 'honestly', so I'm applying a dirty patch. Actually, this is
        // deletion, and it seems unlikely that the exact deletion time be so important.
        model.setModified(ZonedDateTime.now());
        model.setDeleted(true);

        return model;
    }

    @Override
    public void delete(T entity) {

        Assert.notNull(entity, "Cannot delete 'null' entity.");

        deleteById(extractIdFromBean(entity));
    }

    @Override
    public void deleteAll(Iterable<? extends T> entities) {

        Assert.notNull(entities, "Cannot delete 'null' list.");

        // TODO: optimize to one operation (and just one refresh)
        for (T entity : entities) {
            delete(entity);
        }
    }

    @Override
    public Optional<T> findById(String id) {
        return super.findById(id).filter(PersistableCommonModel::isNotDeleted);
    }

    @Override
    public Iterable<T> findAllById(Iterable<String> ids) {
        return StreamUtils.fromIterable(super.findAllById(ids))
                .filter(PersistableCommonModel::isNotDeleted)
                .collect(toList());
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        Query query = new CriteriaQuery(softDeletion.notDeleted());
        query.setPageable(pageable);
        SearchHits<T> searchHits = operations.search(query, getEntityClass(), entityInformation.getIndexCoordinates());
        return searchHitsToPage(searchHits);
    }

    private PageImpl<T> searchHitsToPage(SearchHits<T> searchHits) {
        List<T> entities = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(toList());
        return new PageImpl<>(entities);
    }

    @Override
    public long count() {
        return operations.count(new CriteriaQuery(softDeletion.notDeleted()), getEntityClass());
    }
}
