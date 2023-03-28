package io.extremum.elasticsearch.springdata.repository;

import io.extremum.common.utils.StreamUtils;
import io.extremum.datetime.ApiDateTimeFormat;
import io.extremum.elasticsearch.dao.ElasticsearchCommonDao;
import io.extremum.elasticsearch.dao.SearchOptions;
import io.extremum.elasticsearch.model.ElasticsearchCommonModel;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.data.elasticsearch.core.query.UpdateResponse;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchEntityInformation;
import org.springframework.data.elasticsearch.repository.support.SimpleElasticsearchRepository;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author rpuch
 */
abstract class BaseElasticsearchRepository<T extends ElasticsearchCommonModel>
        extends SimpleElasticsearchRepository<T, String> implements ElasticsearchCommonDao<T> {
    private static final String PAINLESS_LANGUAGE = "painless";

    private static final String MODIFIED = ElasticsearchCommonModel.FIELDS.modified.name();

    private static final String ANALYZER_KEYWORD = "keyword";

    private final ElasticsearchEntityInformation<T, String> metadata;

    private final ApiDateTimeFormat dateTimeFormat = new ApiDateTimeFormat();

    BaseElasticsearchRepository(ElasticsearchEntityInformation<T, String> metadata,
            ElasticsearchOperations elasticsearchOperations) {
        super(metadata, elasticsearchOperations);

        this.metadata = metadata;
    }

    @Override
    public final List<T> findAll() {
        throw new UnsupportedOperationException("" +
                "Please do not call this method as a list of all documents may be very large");
    }

    @Override
    public final Iterable<T> findAll(Sort sort) {
        throw new UnsupportedOperationException("" +
                "Please do not call this method as a list of all documents may be very large");
    }

    private <S> List<S> iterableToList(Iterable<S> iterable) {
        return StreamUtils.fromIterable(iterable).collect(Collectors.toList());
    }

    @Override
    public boolean existsById(String id) {
        return findById(id).isPresent();
    }

    @Override
    public <S extends T> List<S> saveAll(Iterable<S> entities) {
        Iterable<S> savedEntities = super.saveAll(entities);
        return iterableToList(savedEntities);
    }

    @Override
    public final void deleteAll() {
        throw new UnsupportedOperationException("We don't allow to delete all the documents in one go");
    }

    @Override
    public List<T> search(String queryString, SearchOptions searchOptions) {
        QueryStringQueryBuilder query = QueryBuilders.queryStringQuery(queryString);
        if (searchOptions.isExactFieldValueMatch()) {
            query.analyzer(ANALYZER_KEYWORD);
        }

        Iterable<T> results = search(query);
        return iterableToList(results);
    }

    @Override
    public boolean patch(String id, String painlessScript) {
        return patch(id, painlessScript, Collections.emptyMap());
    }

    @Override
    public boolean patch(String id, String painlessScript, Map<String, Object> scriptParams) {
        UpdateRequest updateRequest = new UpdateRequest(metadata.getIndexCoordinates().getIndexName(), id);
        Script script = createScript(painlessScript, scriptParams);
        updateRequest.script(script);

        UpdateQuery updateQuery = UpdateQuery.builder(id)
                .withLang(PAINLESS_LANGUAGE)
                .withScript(amendWithModificationTimeChange(painlessScript))
                .withParams(scriptParams(scriptParams))
                .build();

        UpdateResponse updateResponse = operations.update(updateQuery, entityInformation.getIndexCoordinates());

        refresh();

        if (updateResponse.getResult() != UpdateResponse.Result.UPDATED) {
            throw new UpdateFailedException("Update result is not UPDATED but " + updateResponse.getResult());
        }

        return true;
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
