package io.extremum.elasticsearch.dao;

import io.extremum.common.dao.CommonDao;
import io.extremum.elasticsearch.model.ElasticsearchCommonModel;

import java.util.List;
import java.util.Map;

public interface ElasticsearchCommonDao<M extends ElasticsearchCommonModel> extends CommonDao<M, String> {

    List<M> search(String queryString, SearchOptions searchOptions);

    boolean patch(String id, String painlessScript);

    boolean patch(String id, String painlessScript, Map<String, Object> scriptParams);
}
