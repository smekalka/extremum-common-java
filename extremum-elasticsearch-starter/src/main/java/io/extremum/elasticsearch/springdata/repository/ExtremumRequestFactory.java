package io.extremum.elasticsearch.springdata.repository;

import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Requests;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.springframework.dao.UncategorizedDataAccessException;
import org.springframework.data.elasticsearch.UncategorizedElasticsearchException;
import org.springframework.data.elasticsearch.core.RefreshPolicy;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.BulkOptions;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The code is borrowed from {@link org.springframework.data.elasticsearch.core.RequestFactory}.
 *
 * @author rpuch
 */
// TODO: remove it when the code for filling version, seq_no and primary_key makes it to spring-data-elasticsearch
public class ExtremumRequestFactory {
    private final ElasticsearchConverter elasticsearchConverter;

    public ExtremumRequestFactory(ElasticsearchConverter elasticsearchConverter) {
        this.elasticsearchConverter = elasticsearchConverter;
    }

    public IndexRequest indexRequest(IndexQuery query, IndexCoordinates index) {
        String indexName = index.getIndexName();

        IndexRequest indexRequest;

        if (query.getObject() != null) {
            String id = StringUtils.isEmpty(query.getId()) ? getPersistentEntityId(query.getObject()) : query.getId();
            // If we have a query id and a document id, do not ask ES to generate one.
            if (id != null) {
                indexRequest = new IndexRequest(indexName).id(id);
            } else {
                indexRequest = new IndexRequest(indexName);
            }
            indexRequest.source(elasticsearchConverter.mapObject(query.getObject()).toJson(),
                    Requests.INDEX_CONTENT_TYPE);
        } else if (query.getSource() != null) {
            indexRequest = new IndexRequest(indexName).id(query.getId()).source(query.getSource(),
                    Requests.INDEX_CONTENT_TYPE);
        } else {
            throw new UncategorizedElasticsearchException(
                    "object or source is null, failed to index the document [id: " + query.getId() + ']',
                    new IllegalStateException(
                            "object or source is null, failed to index the document [id: " + query.getId() + ']')
            );
        }

        if (query.getVersion() != null) {
            indexRequest.version(query.getVersion());
            VersionType versionType = retrieveVersionTypeFromPersistentEntity(query.getObject().getClass());
            indexRequest.versionType(versionType);
        }

        if (query.getSeqNo() != null) {
            indexRequest.setIfSeqNo(query.getSeqNo());
        }

        if (query.getPrimaryTerm() != null) {
            indexRequest.setIfPrimaryTerm(query.getPrimaryTerm());
        }

        return indexRequest;
    }

    @Nullable
    private String getPersistentEntityId(Object entity) {

        Object identifier = elasticsearchConverter.getMappingContext() //
                .getRequiredPersistentEntity(entity.getClass()) //
                .getIdentifierAccessor(entity).getIdentifier();

        if (identifier != null) {
            return identifier.toString();
        }

        return null;
    }

    private VersionType retrieveVersionTypeFromPersistentEntity(Class<?> clazz) {

        if (clazz != null) {
            return VersionType.valueOf(elasticsearchConverter.getMappingContext().getRequiredPersistentEntity(clazz).getVersionType().name());
        }
        return VersionType.EXTERNAL;
    }

    public BulkRequest bulkRequest(List<?> queries, BulkOptions bulkOptions, IndexCoordinates index) {
        BulkRequest bulkRequest = new BulkRequest();

        if (bulkOptions.getTimeout() != null) {
            bulkRequest.timeout(new TimeValue(bulkOptions.getTimeout().toMillis()));
        }

        if (bulkOptions.getRefreshPolicy() != null) {
            bulkRequest.setRefreshPolicy(String.valueOf(bulkOptions.getRefreshPolicy()));
        }

        if (bulkOptions.getWaitForActiveShards() != null) {
            bulkRequest.waitForActiveShards(bulkOptions.getWaitForActiveShards().getValue());
        }

        if (bulkOptions.getPipeline() != null) {
            bulkRequest.pipeline(bulkOptions.getPipeline());
        }

        if (bulkOptions.getRoutingId() != null) {
            bulkRequest.routing(bulkOptions.getRoutingId());
        }

        queries.forEach(query -> {

            if (query instanceof IndexQuery) {
                bulkRequest.add(indexRequest((IndexQuery) query, index));
            } else if (query instanceof UpdateQuery) {
                bulkRequest.add(updateRequest((UpdateQuery) query, index));
            }
        });
        return bulkRequest;
    }

    public UpdateRequest updateRequest(UpdateQuery query, IndexCoordinates index) {

        UpdateRequest updateRequest = new UpdateRequest(index.getIndexName(), query.getId());

        if (query.getScript() != null) {
            Map<String, Object> params = query.getParams();

            if (params == null) {
                params = new HashMap<>();
            }
            Script script = new Script(ScriptType.INLINE, query.getLang(), query.getScript(), params);
            updateRequest.script(script);
        }

        if (query.getDocument() != null) {
            updateRequest.doc(query.getDocument());
        }

        if (query.getUpsert() != null) {
            updateRequest.upsert(query.getUpsert());
        }

        if (query.getRouting() != null) {
            updateRequest.routing(query.getRouting());
        }

        if (query.getScriptedUpsert() != null) {
            updateRequest.scriptedUpsert(query.getScriptedUpsert());
        }

        if (query.getDocAsUpsert() != null) {
            updateRequest.docAsUpsert(query.getDocAsUpsert());
        }

        if (query.getFetchSource() != null) {
            updateRequest.fetchSource(query.getFetchSource());
        }

        if (query.getFetchSourceIncludes() != null || query.getFetchSourceExcludes() != null) {
            List<String> includes = query.getFetchSourceIncludes() != null ? query.getFetchSourceIncludes()
                    : Collections.emptyList();
            List<String> excludes = query.getFetchSourceExcludes() != null ? query.getFetchSourceExcludes()
                    : Collections.emptyList();
            updateRequest.fetchSource(includes.toArray(new String[0]), excludes.toArray(new String[0]));
        }

        if (query.getIfSeqNo() != null) {
            updateRequest.setIfSeqNo(query.getIfSeqNo());
        }

        if (query.getIfPrimaryTerm() != null) {
            updateRequest.setIfPrimaryTerm(query.getIfPrimaryTerm());
        }

        if (query.getRefreshPolicy() != null) {
            updateRequest.setRefreshPolicy(query.getRefreshPolicy().name().toLowerCase());
        }

        if (query.getRetryOnConflict() != null) {
            updateRequest.retryOnConflict(query.getRetryOnConflict());
        }

        if (query.getTimeout() != null) {
            updateRequest.timeout(query.getTimeout());
        }

        if (query.getWaitForActiveShards() != null) {
            updateRequest.waitForActiveShards(ActiveShardCount.parseString(query.getWaitForActiveShards()));
        }

        return updateRequest;
    }
}
