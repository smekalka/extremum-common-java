package io.extremum.common.utils;

import io.extremum.mongo.dao.impl.SpringDataMongoCommonDao;
import io.extremum.mongo.model.MongoCommonModel;
import org.springframework.data.repository.support.Repositories;

public class RepositoryUtils {
    @SuppressWarnings("unchecked")
    public static <Model extends MongoCommonModel> SpringDataMongoCommonDao<Model> getMongoRepositoryFor(Class<Model> modelClass, Repositories repositories) {
        return repositories.getRepositoryFor(modelClass)
                .map(o -> (SpringDataMongoCommonDao<Model>) o)
                .orElseThrow(() -> new RuntimeException("Cannot find Repository for model class: " + modelClass));
    }
}
