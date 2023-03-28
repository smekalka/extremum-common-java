package io.extremum.graphql.resolver;

import graphql.schema.DataFetchingEnvironment;
import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.common.model.CollectionFilter;
import io.extremum.everything.services.management.ModelNames;
import io.extremum.graphql.dao.AdvancedCommonDao;
import io.extremum.graphql.model.PagingAndSortingRequest;
import io.extremum.graphql.model.relay.DefaultPaginatedDataConnection;
import io.extremum.security.DataSecurity;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.basic.MultilingualLanguage;
import lombok.AllArgsConstructor;
import org.apache.http.HttpHeaders;
import org.springframework.data.domain.Page;

import javax.servlet.http.HttpServletRequest;

@AllArgsConstructor
public abstract class AbstractQueryResolver {

    private final ModelNames modelNames;
    private final DataSecurity dataSecurity;
    private final AdvancedCommonDao commonGraphQLDao;

    protected <T extends BasicModel<?>> DefaultPaginatedDataConnection<T> page(PagingAndSortingRequest paging, String collectionName) {
        String modelName = modelNames.determineModelNameByCollectionName(collectionName);
        if (modelName == null) {
            throw new IllegalArgumentException(String.format("Model collection with name %s not found", collectionName));
        }

        Page<T> models = commonGraphQLDao.findAll(modelName, paging);
        if (!models.getContent().isEmpty()) {
            dataSecurity.checkGetAllowed(models.getContent().get(0));
        }

        return new DefaultPaginatedDataConnection<>(models);
    }


    protected <T extends BasicModel<?>> DefaultPaginatedDataConnection<T> page(PagingAndSortingRequest paging, String filter, String collectionName, DataFetchingEnvironment env) {
        String modelName = modelNames.determineModelNameByCollectionName(collectionName);
        if (modelName == null) {
            throw new IllegalArgumentException(String.format("Model collection with name %s not found", collectionName));
        }

        String acceptLanguage = ((HttpServletRequest) env.getGraphQlContext().get(HttpServletRequest.class)).getHeader(HttpHeaders.ACCEPT_LANGUAGE);
        Page<T> models = commonGraphQLDao.findAll(modelName, new CollectionFilter(filter, MultilingualLanguage.fromString(acceptLanguage)), paging);
        if (!models.getContent().isEmpty()) {
            dataSecurity.checkGetAllowed(models.getContent().get(0));
        }

        return new DefaultPaginatedDataConnection<>(models);
    }


    protected <T extends Model> T one(String id) {
        T model = commonGraphQLDao.get(id);
        dataSecurity.checkGetAllowed(model);
        return model;
    }
}
