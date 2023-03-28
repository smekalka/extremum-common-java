package io.extremum.mongo.dao;

import io.extremum.common.dao.CommonDao;
import io.extremum.mongo.model.MongoCommonModel;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MongoCommonDao<M extends MongoCommonModel> extends CommonDao<M, ObjectId> {
    List<M> listByParameters(Map<String, Object> parameters);

    List<M> listByFieldValue(String fieldName, Object fieldValue);

    Optional<M> getSelectedFieldsById(ObjectId id, String... fieldNames);
}
