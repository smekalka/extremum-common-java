package io.extremum.mongo.service;

import io.extremum.common.service.CommonService;
import io.extremum.common.service.Problems;
import io.extremum.mongo.model.MongoCommonModel;

import java.util.List;
import java.util.Map;

/**
 * Common interface for mongo services
 */
public interface MongoCommonService<M extends MongoCommonModel> extends CommonService<M> {

    List<M> listByParameters(Map<String, Object> parameters);

    List<M> listByParameters(Map<String, Object> parameters, Problems problems);

    List<M> listByFieldValue(String fieldName, Object fieldValue);

    List<M> listByFieldValue(String fieldName, Object fieldValue, Problems problems);

    List<M> listByFieldValue(String fieldName, Object fieldValue, int offset, int limit);

    List<M> listByFieldValue(String fieldName, Object fieldValue, int offset, int limit, Problems problems);

    M getSelectedFieldsById(String id, String... fieldNames);

    M getSelectedFieldsById(String id, Problems problems, String... fieldNames);
}
