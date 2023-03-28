package io.extremum.elasticsearch.springdata.repository;

import io.extremum.elasticsearch.model.ElasticsearchCommonModel;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.BulkOptions;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SeqNoPrimaryTerm;
import org.springframework.data.util.Streamable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author rpuch
 */
public class ExtremumElasticsearchRestTemplate extends ElasticsearchRestTemplate {
    private ExtremumRequestFactory customizedRequestFactory;

    public ExtremumElasticsearchRestTemplate(RestHighLevelClient client, ElasticsearchConverter elasticsearchConverter) {
        super(client, elasticsearchConverter);
    }

    @Override
    protected void initialize(ElasticsearchConverter elasticsearchConverter) {
        super.initialize(elasticsearchConverter);

        customizedRequestFactory = new ExtremumRequestFactory(elasticsearchConverter);
    }

    // The following methods (#index() and #save() are only overriden to support setting version, seq_no and primary_key
    // after indexing. When this functionality makes it to the standard spring-data-elasticsearch, these overrides
    // should be removed.

    @Override
    public String index(IndexQuery query, IndexCoordinates index) {

        maybeCallbackBeforeConvertWithQuery(query, index);

        IndexRequest request = customizedRequestFactory.indexRequest(query, index);
        IndexResponse response = execute(client -> client.index(request, RequestOptions.DEFAULT));
        String documentId = response.getId();

        // We should call this because we are not going through a mapper.
        Object queryObject = query.getObject();
        if (queryObject != null) {
            setPersistentEntityId(queryObject, documentId);
            // PATCH: this is the only change we have here: we also set version, seq_no, primary_term
            updateIndexedObject(queryObject, response.getVersion(), response.getSeqNo(), response.getPrimaryTerm());
        }

        maybeCallbackAfterSaveWithQuery(query, index);

        return documentId;
    }

    private void updateIndexedObject(@NonNull Object queryObject, long version, long seqNo, long primaryTerm) {
        if (queryObject instanceof ElasticsearchCommonModel) {
            ElasticsearchCommonModel model = (ElasticsearchCommonModel) queryObject;
            model.setVersion(version);
            model.setSeqNoPrimaryTerm(new SeqNoPrimaryTerm(seqNo, primaryTerm));
        }
    }

    @Override
    public <T> Iterable<T> save(Iterable<T> entities, IndexCoordinates index) {

        Assert.notNull(entities, "entities must not be null");
        Assert.notNull(index, "index must not be null");

        List<IndexQuery> indexQueries = Streamable.of(entities).stream().map(this::getIndexQuery)
                .collect(Collectors.toList());

        if (!indexQueries.isEmpty()) {
            // PATCH: we obtain a bulk response to set version seq_no, primary_term
            BulkResponse bulkResponse = doBulkOperation(indexQueries, BulkOptions.defaultOptions(), index);
            int i = 0;
            for (T entity : entities) {
                BulkItemResponse item = bulkResponse.getItems()[i++];
                DocWriteResponse response = item.getResponse();
                setPersistentEntityId(entity, item.getId());
                updateIndexedObject(entity, response.getVersion(), response.getSeqNo(), response.getPrimaryTerm());
            };
        }

        return indexQueries.stream().map(IndexQuery::getObject).map(entity -> (T) entity).collect(Collectors.toList());
    }

    private <T> IndexQuery getIndexQuery(T entity) {
        String id = getEntityId(entity);

        if (id != null) {
            id = elasticsearchConverter.convertId(id);
        }

        IndexQueryBuilder builder = new IndexQueryBuilder() //
                .withId(id) //
                .withObject(entity);
        SeqNoPrimaryTerm seqNoPrimaryTerm = getEntitySeqNoPrimaryTerm(entity);
        if (seqNoPrimaryTerm != null) {
            builder.withSeqNoPrimaryTerm(seqNoPrimaryTerm);
        } else {
            // version cannot be used together with seq_no and primary_term
            builder.withVersion(getEntityVersion(entity));
        }
        return builder.build();
    }

    @Nullable
    private String getEntityId(Object entity) {
        ElasticsearchPersistentEntity<?> persistentEntity = getRequiredPersistentEntity(entity.getClass());
        ElasticsearchPersistentProperty idProperty = persistentEntity.getIdProperty();

        if (idProperty != null) {
            return stringIdRepresentation(persistentEntity.getPropertyAccessor(entity).getProperty(idProperty));
        }

        return null;
    }

    @Nullable
    private Long getEntityVersion(Object entity) {
        ElasticsearchPersistentEntity<?> persistentEntity = getRequiredPersistentEntity(entity.getClass());
        ElasticsearchPersistentProperty versionProperty = persistentEntity.getVersionProperty();

        if (versionProperty != null) {
            Object version = persistentEntity.getPropertyAccessor(entity).getProperty(versionProperty);

            if (version != null && Long.class.isAssignableFrom(version.getClass())) {
                return ((Long) version);
            }
        }

        return null;
    }

    @Nullable
    private SeqNoPrimaryTerm getEntitySeqNoPrimaryTerm(Object entity) {
        ElasticsearchPersistentEntity<?> persistentEntity = getRequiredPersistentEntity(entity.getClass());
        ElasticsearchPersistentProperty property = persistentEntity.getSeqNoPrimaryTermProperty();

        if (property != null) {
            Object seqNoPrimaryTerm = persistentEntity.getPropertyAccessor(entity).getProperty(property);

            if (seqNoPrimaryTerm != null && SeqNoPrimaryTerm.class.isAssignableFrom(seqNoPrimaryTerm.getClass())) {
                return (SeqNoPrimaryTerm) seqNoPrimaryTerm;
            }
        }

        return null;
    }

    ElasticsearchPersistentEntity<?> getRequiredPersistentEntity(Class<?> clazz) {
        return elasticsearchConverter.getMappingContext().getRequiredPersistentEntity(clazz);
    }

    private BulkResponse doBulkOperation(List<?> queries, BulkOptions bulkOptions, IndexCoordinates index) {
        maybeCallbackBeforeConvertWithQueries(queries, index);
        BulkRequest bulkRequest = customizedRequestFactory.bulkRequest(queries, bulkOptions, index);
        BulkResponse bulkResponse = execute(client -> client.bulk(bulkRequest, RequestOptions.DEFAULT));
        checkForBulkOperationFailure(bulkResponse);
        maybeCallbackAfterSaveWithQueries(queries, index);
        return bulkResponse;
    }
}
