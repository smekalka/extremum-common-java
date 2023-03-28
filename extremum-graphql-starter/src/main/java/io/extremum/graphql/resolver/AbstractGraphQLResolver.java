package io.extremum.graphql.resolver;

import graphql.schema.DataFetchingEnvironment;
import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.common.model.CollectionFilter;
import io.extremum.common.spring.data.OffsetBasedPageRequest;
import io.extremum.graphql.dao.AdvancedCommonDao;
import io.extremum.graphql.model.PagingAndSortingRequest;
import io.extremum.graphql.model.relay.DefaultPaginatedDataConnection;
import io.extremum.security.DataSecurity;
import io.extremum.sharedmodels.basic.MultilingualLanguage;
import org.apache.http.HttpHeaders;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Transactional
public abstract class AbstractGraphQLResolver {

    private final DataSecurity dataSecurity;
    private final AdvancedCommonDao commonGraphQLDao;

    protected AbstractGraphQLResolver(DataSecurity dataSecurity, AdvancedCommonDao commonGraphQLDao) {
        this.dataSecurity = dataSecurity;
        this.commonGraphQLDao = commonGraphQLDao;
    }

    @NotNull
    public <Folder extends BasicModel<?>, Item> DefaultPaginatedDataConnection<Item> composeConnection(Folder model, List<Item> collection, Class<Item> nestedClass, PagingAndSortingRequest paging, String collectionName) {
        dataSecurity.checkGetAllowed(model);
        List<Item> objects = commonGraphQLDao.getNestedCollection(model, nestedClass, collectionName, paging);

        return composeConnection(collection.size(), paging, objects);
    }

    @NotNull
    public <Folder extends BasicModel<?>, Item> DefaultPaginatedDataConnection<Item> composeConnection(
            Folder model,
            String filter,
            PagingAndSortingRequest paging,
            String collectionName,
            DataFetchingEnvironment env
    ) {
        dataSecurity.checkGetAllowed(model);

        String acceptLanguage = ((HttpServletRequest) env.getGraphQlContext().get(HttpServletRequest.class)).getHeader(HttpHeaders.ACCEPT_LANGUAGE);
        Page<Item> objects = commonGraphQLDao.getNestedCollection(model, new CollectionFilter(filter, MultilingualLanguage.fromString(acceptLanguage)), collectionName, paging);

        return composeConnection(objects.getTotalElements(), paging, objects.getContent());
    }

    @NotNull
    private <Item> DefaultPaginatedDataConnection<Item> composeConnection(long size, PagingAndSortingRequest paging, List<Item> models) {
        return new DefaultPaginatedDataConnection<>(
                new PageImpl<>(models, new OffsetBasedPageRequest(paging.getOffset(), paging.getLimit()), size)
        );
    }


    public <Folder extends BasicModel<?>, Item> void addToCollection(Folder model, List<Item> input, String collectionName) {
        dataSecurity.checkPatchAllowed(model);
        commonGraphQLDao.addToNestedCollection(model, input, collectionName);
    }

    public <Folder extends BasicModel<?>, Item> void removeFromCollection(Folder model, List<Item> input, String collectionName) {
        dataSecurity.checkPatchAllowed(model);
        commonGraphQLDao.removeFromNestedCollection(model, input, collectionName);
    }
}