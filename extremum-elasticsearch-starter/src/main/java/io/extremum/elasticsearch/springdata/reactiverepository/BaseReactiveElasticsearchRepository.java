package io.extremum.elasticsearch.springdata.reactiverepository;

import io.extremum.datetime.ApiDateTimeFormat;
import io.extremum.elasticsearch.dao.ReactiveElasticsearchCommonDao;
import io.extremum.elasticsearch.dao.SearchOptions;
import io.extremum.elasticsearch.model.ElasticsearchCommonModel;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchEntityInformation;
import org.springframework.data.elasticsearch.repository.support.SimpleReactiveElasticsearchRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.*;

/**
 * @author rpuch
 */
abstract class BaseReactiveElasticsearchRepository<T extends ElasticsearchCommonModel>
        extends SimpleReactiveElasticsearchRepository<T, String>
        implements ReactiveElasticsearchCommonDao<T> {
    private static final String PAINLESS_LANGUAGE = "painless";

    private static final String MODIFIED = ElasticsearchCommonModel.FIELDS.modified.name();

    private static final String ANALYZER_KEYWORD = "keyword";

    private final ElasticsearchEntityInformation<T, String> entityInformation;
    private final ReactiveElasticsearchOperations elasticsearchOperations;

    private final ApiDateTimeFormat dateTimeFormat = new ApiDateTimeFormat();

    BaseReactiveElasticsearchRepository(ElasticsearchEntityInformation<T, String> entityInformation,
                                        ReactiveElasticsearchOperations elasticsearchOperations) {
        super(entityInformation, elasticsearchOperations);

        this.entityInformation = entityInformation;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public final Flux<T> findAll() {
        throw new UnsupportedOperationException("" +
                "Please do not call this method as a list of all documents may be very large");
    }

    @Override
    public final Flux<T> findAll(Sort sort) {
        throw new UnsupportedOperationException("" +
                "Please do not call this method as a list of all documents may be very large");
    }

    @Override
    public final Mono<Void> deleteAll() {
        throw new UnsupportedOperationException("We don't allow to delete all the documents in one go");
    }

    @Override
    public Flux<T> search(String queryString, SearchOptions searchOptions) {
        QueryStringQueryBuilder query = QueryBuilders.queryStringQuery(queryString);
        if (searchOptions.isExactFieldValueMatch()) {
            query.analyzer(ANALYZER_KEYWORD);
        }

        return search(query);
    }

    protected Flux<T> search(QueryBuilder query) {
        Query searchQuery = new NativeSearchQueryBuilder().withQuery(query).build();
        return elasticsearchOperations.search(searchQuery, entityInformation.getJavaType())
                .map(SearchHit::getContent);
    }

    @Override
    public Mono<Boolean> patch(String id, String painlessScript) {
        return patch(id, painlessScript, Collections.emptyMap());
    }

    @Override
    public Mono<Boolean> patch(String id, String painlessScript, Map<String, Object> scriptParams) {
        UpdateRequest updateRequest = new UpdateRequest(entityInformation.getIndexCoordinates().getIndexName(), id);
        Script script = createScript(painlessScript, scriptParams);
        updateRequest.script(script);

        UpdateQuery updateQuery = UpdateQuery.builder(id)
                .withLang(PAINLESS_LANGUAGE)
                .withScript(amendWithModificationTimeChange(painlessScript))
                .withParams(scriptParams(scriptParams))
                .build();

        // TODO: use single update() method instead of bulkUpdate() when the former is available
        return elasticsearchOperations.bulkUpdate(singletonList(updateQuery), entityInformation.getIndexCoordinates())
                .thenReturn(true);
    }

    private Script createScript(String painlessScript, Map<String, Object> params) {
        String scriptWithModificationTimeChange = amendWithModificationTimeChange(painlessScript);
        Map<String, Object> paramsWithModificationTimeChange = scriptParams(params);
        return new Script(ScriptType.INLINE, PAINLESS_LANGUAGE, scriptWithModificationTimeChange,
                paramsWithModificationTimeChange);
    }

    private Map<String, Object> scriptParams(Map<String, Object> params) {
        return amendWithModificationTime(params);
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
        return dateTimeFormat.format(ZonedDateTime.now());
    }

}
