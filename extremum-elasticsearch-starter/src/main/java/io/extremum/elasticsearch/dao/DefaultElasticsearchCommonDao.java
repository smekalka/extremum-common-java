package io.extremum.elasticsearch.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.common.model.PersistableCommonModel;
import io.extremum.common.model.PersistableCommonModel.FIELDS;
import io.extremum.common.utils.CollectionUtils;
import io.extremum.common.utils.ModelUtils;
import io.extremum.common.utils.StreamUtils;
import io.extremum.datetime.ApiDateTimeFormat;
import io.extremum.datetime.DateUtils;
import io.extremum.elasticsearch.dao.extractor.AccessorFacade;
import io.extremum.elasticsearch.dao.extractor.GetResponseAccessorFacade;
import io.extremum.elasticsearch.dao.extractor.SearchHitAccessorFacade;
import io.extremum.elasticsearch.facilities.ElasticsearchDescriptorFacilities;
import io.extremum.elasticsearch.model.ElasticsearchCommonModel;
import io.extremum.elasticsearch.properties.ElasticsearchProperties;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.springframework.data.elasticsearch.core.query.SeqNoPrimaryTerm;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Arrays.*;
import static java.util.Optional.*;
import static org.apache.http.HttpStatus.*;

@Slf4j
public class DefaultElasticsearchCommonDao<M extends ElasticsearchCommonModel> implements ElasticsearchCommonDao<M> {
    private static final String MODIFIED = ElasticsearchCommonModel.FIELDS.modified.name();
    private static final String DELETED = ElasticsearchCommonModel.FIELDS.deleted.name();

    private static final String PAINLESS_LANGUAGE = "painless";

    private static final String DELETE_DOCUMENT_PAINLESS_SCRIPT = "ctx._source.deleted = params.deleted";

    private static final String ANALYZER_KEYWORD = "keyword";

    private final RestClientBuilder restClientBuilder;
    private final DescriptorService descriptorService;
    private final ElasticsearchDescriptorFacilities descriptorFacilities;

    private final ObjectMapper mapper;
    private final String indexName;
    private final String indexType;

    private final Class<? extends M> modelClass;

    private final ApiDateTimeFormat apiDateTimeFormat = new ApiDateTimeFormat();

    protected DefaultElasticsearchCommonDao(Class<M> modelClass, ElasticsearchProperties elasticsearchProperties,
                                            DescriptorService descriptorService,
                                            ElasticsearchDescriptorFacilities descriptorFacilities, ObjectMapper mapper, String indexName,
                                            String indexType) {
        this.modelClass = modelClass;
        this.descriptorService = descriptorService;
        this.descriptorFacilities = descriptorFacilities;
        this.mapper = mapper;
        this.indexName = indexName;
        this.indexType = indexType;

        restClientBuilder = initRest(elasticsearchProperties);
    }

    protected DefaultElasticsearchCommonDao(ElasticsearchProperties elasticsearchProperties,
                                            DescriptorService descriptorService,
                                            ElasticsearchDescriptorFacilities descriptorFactory,
                                            ObjectMapper mapper, String indexName, String indexType) {
        this.descriptorService = descriptorService;
        this.descriptorFacilities = descriptorFactory;
        this.mapper = mapper;
        this.indexName = indexName;
        this.indexType = indexType;

        restClientBuilder = initRest(elasticsearchProperties);
        this.modelClass = (Class<M>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    private static RestClientBuilder initRest(ElasticsearchProperties properties) {
        if (CollectionUtils.isNullOrEmpty(properties.getHosts())) {
            log.error("Unable to configure {} because list of hosts is empty", RestClientBuilder.class.getName());
            throw new RuntimeException("Unable to configure " + RestClientBuilder.class.getName() +
                    " because list of hosts is empty");
        }

        List<HttpHost> httpHosts = properties.getHosts().stream()
                .map(h -> new HttpHost(h.getHost(), h.getPort(), h.getProtocol()))
                .collect(Collectors.toList());

        RestClientBuilder restClientBuilder = RestClient.builder(httpHosts.toArray(new HttpHost[]{}));

        if (properties.getUsername() != null && properties.getPassword() != null) {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(properties.getUsername(), properties.getPassword()));

            restClientBuilder.setHttpClientConfigCallback(httpAsyncClientBuilder ->
                    httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
        }

        return restClientBuilder;
    }

    private RestHighLevelClient getClient() {
        return new RestHighLevelClient(restClientBuilder);
    }

    @Override
    public List<M> search(String queryString, SearchOptions searchOptions) {
        final SearchRequest request = new SearchRequest(indexName);

        QueryStringQueryBuilder contentQuery = createContentQuery(queryString, searchOptions);
        QueryBuilder totalQuery = QueryBuilders.boolQuery()
                .must(contentQuery)
                .mustNot(QueryBuilders.queryStringQuery("true").field(DELETED).defaultOperator(Operator.AND));
        request.source(
                new SearchSourceBuilder()
                        .query(totalQuery)
                        .version(true)
                        .seqNoAndPrimaryTerm(true)
        );

        return doSearch(queryString, request);
    }

    private QueryStringQueryBuilder createContentQuery(String queryString, SearchOptions searchOptions) {
        QueryStringQueryBuilder contentQuery = QueryBuilders.queryStringQuery(queryString);
        if (searchOptions.isExactFieldValueMatch()) {
            contentQuery.analyzer(ANALYZER_KEYWORD);
        }
        return contentQuery;
    }

    protected List<M> doSearch(String queryString, SearchRequest request) {
        try (RestHighLevelClient client = getClient()) {
            return executeSearch(queryString, request, client);
        } catch (IOException e) {
            log.error("Unable to search by query {}", queryString, e);
            throw new RuntimeException("Unable to search by query " + queryString, e);
        }
    }

    private List<M> executeSearch(String queryString, SearchRequest request,
                                  RestHighLevelClient client) throws IOException {
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        if (HttpStatus.SC_OK != response.status().getStatus()) {
            log.error("Unable to perform search by query {}: {}", queryString, response.status());
            throw new RuntimeException("Nothing found by query " + queryString);
        }

        return StreamUtils.fromIterable(response.getHits())
                .map(hit -> extract(new SearchHitAccessorFacade(hit, descriptorService)))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<M> findById(String id) {
        try (RestHighLevelClient client = getClient()) {
            return executeFindById(id, client);
        } catch (IOException e) {
            log.error("Unable to get data by id {} from index {} with type {}",
                    id, indexName, indexType, e);
            throw new RuntimeException("Unable to get data by id " + id +
                    " from index " + indexName +
                    " with type " + indexType,
                    e);
        }
    }

    private Optional<M> executeFindById(String id, RestHighLevelClient client) throws IOException {
        GetResponse response = client.get(new GetRequest(indexName, id), RequestOptions.DEFAULT);

        if (!response.isExists()) {
            return Optional.empty();
        }

        Map<String, Object> sourceMap = response.getSourceAsMap();

        if (sourceMap.getOrDefault(FIELDS.deleted.name(), false).equals(true)) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(extract(new GetResponseAccessorFacade(response, descriptorService)));
        }
    }

    @Override
    public boolean existsById(String id) {
        if (!isDocumentExists(id)) {
            return false;
        }

        return findById(id)
                .map(PersistableCommonModel::isNotDeleted)
                .orElse(false);
    }

    protected boolean isDocumentExists(String id) {
        try (RestHighLevelClient client = getClient()) {
            return executeExists(id, client);
        } catch (IOException e) {
            log.error("Unable to check exists data by id {}", id, e);
            throw new RuntimeException("Unable to check data exists by id " + id, e);
        }
    }

    private boolean executeExists(String id, RestHighLevelClient client) throws IOException {
        GetRequest getRequest = new GetRequest(indexName, id);
        getRequest.fetchSourceContext(FetchSourceContext.DO_NOT_FETCH_SOURCE);
        getRequest.storedFields("_none_");

        GetResponse response = client.get(getRequest, RequestOptions.DEFAULT);
        return response.isExists();
    }

    @Override
    public <N extends M> N save(N model) {
        preSave(model);
        N saved = doSave(model);
        refreshIndex();
        return saved;
    }

    protected <N extends M> N doSave(N model) {
        try (RestHighLevelClient client = getClient()) {
            return executeSave(model, client);
        } catch (IOException e) {
            log.error("Unable to add data to index", e);
            throw new RuntimeException("Unable to add data to index", e);
        }
    }

    private <N extends M> N executeSave(N model, RestHighLevelClient client) throws IOException {
        final IndexRequest request = Requests.indexRequest(indexName).id(model.getId());

        request.source(serializeModel(model), XContentType.JSON);

        if (model.getSeqNoPrimaryTerm() != null) {
            request.setIfSeqNo(model.getSeqNoPrimaryTerm().getSequenceNumber());
            request.setIfPrimaryTerm(model.getSeqNoPrimaryTerm().getPrimaryTerm());
        }

        final IndexResponse response = client.index(request, RequestOptions.DEFAULT);

        if (!asList(SC_OK, SC_CREATED).contains(response.status().getStatus())) {
            log.error("Document don't be indexed, status {}", response.status());
            throw new RuntimeException("Document don't be indexed");
        }

        if (response.getSeqNo() >= 0 && response.getPrimaryTerm() > 0) {
            model.setSeqNoPrimaryTerm(new SeqNoPrimaryTerm(response.getSeqNo(), response.getPrimaryTerm()));
        }
        model.setVersion(response.getVersion());
        return model;
    }

    protected void preSave(M model) {
        if (model.getId() == null) {
            final Descriptor descriptor = getOrCreateDescriptor(model);

            model.setUuid(descriptor);
            model.setId(descriptor.getInternalId());
            ZonedDateTime now = ZonedDateTime.now();
            model.setCreated(now);
            model.setModified(now);
            model.setDeleted(false);
        } else if (existsById(model.getId())) {
            model.setModified(ZonedDateTime.now());
        } else {
            throw new RuntimeException("Document " + model.getId() + " has been deleted and can't be updated");
        }
    }

    protected void refreshIndex() {
        try (RestHighLevelClient client = getClient()) {
            client.indices().refresh(Requests.refreshRequest(indexName), RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException("Cannot refresh index", e);
        }
    }

    private Descriptor getOrCreateDescriptor(M model) {
        String name = ModelUtils.getModelName(model.getClass());
        String iri = model.getIri();

        if (model.getUuid() != null) {
            return model.getUuid();
        } else {
            return descriptorFacilities.create(newInternalId(), name, iri);
        }
    }

    private UUID newInternalId() {
        return UUID.randomUUID();
    }

    private String serializeModel(M model) {
        try {
            return mapper.writeValueAsString(model);
        } catch (JsonProcessingException e) {
            log.error("Unable to serialize model {}", model, e);
            throw new RuntimeException("Unable to serialize model " + model, e);
        }
    }

    private M deserializeModel(String rawSource) {
        try {
            return mapper.readValue(rawSource, modelClass);
        } catch (IOException e) {
            log.error("Unable to deserialize {} to {}", rawSource, modelClass, e);
            throw new RuntimeException("Unable to deserialize " + rawSource + " to " + modelClass, e);
        }
    }

    @Override
    public <N extends M> List<N> saveAll(Iterable<N> entities) {
        entities.forEach(model -> {
            preSave(model);
            doSave(model);
        });

        refreshIndex();

        return StreamUtils.fromIterable(entities).collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(FIELDS.deleted.name(), true);
        parameters.put(FIELDS.modified.name(), getNowAsString());

        patch(id, DELETE_DOCUMENT_PAINLESS_SCRIPT, parameters);
    }

    @Override
    public M deleteByIdAndReturn(String id) {
        M model = findById(id).orElseThrow(() -> new ModelNotFoundException(modelClass, id));
        deleteById(id);
        return model;
    }

    @Override
    public boolean patch(String id, String painlessScript) {
        return patch(id, painlessScript, Collections.emptyMap());
    }

    @Override
    public boolean patch(String id, String painlessScript, Map<String, Object> scriptParams) {
        if (!existsById(id)) {
            throw new ModelNotFoundException("Not found " + id);
        }

        final UpdateRequest request = new UpdateRequest(indexName, id);
        request.script(createScript(painlessScript, scriptParams));

        try (final RestHighLevelClient client = getClient()) {
            return executeUpdate(request, client);
        } catch (IOException e) {
            log.error("Unable to patch document {}", id, e);
            throw new RuntimeException("Unable to patch document " + id, e);
        }
    }

    private boolean executeUpdate(UpdateRequest request, RestHighLevelClient client) throws IOException {
        final UpdateResponse response = client.update(request, RequestOptions.DEFAULT);

        if (SC_OK == response.status().getStatus()) {
            refreshIndex();
            return true;
        } else {
            log.warn("Document {} is not patched, status {}", request.id(), response.status());
            return false;
        }
    }

    private Script createScript(String painlessScript, Map<String, Object> params) {
        String scriptWithModificationTimeChange = amendWithModificationTimeChange(painlessScript);
        Map<String, Object> paramsWithModificationTimeChange = amendWithModificationTime(params);
        return new Script(ScriptType.INLINE, PAINLESS_LANGUAGE, scriptWithModificationTimeChange,
                paramsWithModificationTimeChange);
    }

    private String amendWithModificationTimeChange(String painlessScript) {
        return painlessScript + changeModificationTimeScriptSuffix();
    }

    private String changeModificationTimeScriptSuffix() {
        return "; ctx._source." + MODIFIED + " = params." + MODIFIED;
    }

    private Map<String, Object> amendWithModificationTime(Map<String, Object> params) {
        Map<String, Object> paramsWithModificationTimeChange = new HashMap<>(params);
        paramsWithModificationTimeChange.put(MODIFIED, getNowAsString());
        return paramsWithModificationTimeChange;
    }

    private String getNowAsString() {
        return DateUtils.formatZonedDateTimeISO_8601(ZonedDateTime.now());
    }

    private M extract(AccessorFacade accessor) {
        M model = deserializeModel(accessor.getRawSource());
        model.setId(accessor.getId());
        model.setUuid(accessor.getUuid());
        model.setVersion(accessor.getVersion());
        if (accessor.getSeqNo() >= 0 && accessor.getPrimaryTerm() > 0) {
            model.setSeqNoPrimaryTerm(new SeqNoPrimaryTerm(accessor.getSeqNo(), accessor.getPrimaryTerm()));
        }

        final Map<String, Object> sourceAsMap = accessor.getSourceAsMap();

        final boolean deleted = ofNullable(sourceAsMap)
                .map(m -> m.get(FIELDS.deleted.name()))
                .map(Boolean.class::cast)
                .orElse(false);

        model.setDeleted(deleted);

        ofNullable(sourceAsMap)
                .map(m -> zonedDateTimeFromMap(m, FIELDS.created.name()))
                .ifPresent(model::setCreated);

        ofNullable(sourceAsMap)
                .map(m -> zonedDateTimeFromMap(m, FIELDS.modified.name()))
                .ifPresent(model::setModified);

        return model;
    }


    private ZonedDateTime zonedDateTimeFromMap(Map<String, Object> map, String fieldName) {
        return ofNullable(map)
                .map(m -> m.get(fieldName))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(apiDateTimeFormat::parse)
                .orElse(null);
    }
}
