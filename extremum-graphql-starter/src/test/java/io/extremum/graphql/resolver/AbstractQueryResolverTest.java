package io.extremum.graphql.resolver;

import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.everything.services.management.ModelNames;
import io.extremum.graphql.dao.AdvancedCommonDao;
import io.extremum.graphql.model.PagingAndSortingRequest;
import io.extremum.graphql.model.relay.DefaultPaginatedDataConnection;
import io.extremum.security.DataSecurity;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AbstractQueryResolverTest {

    @Mock
    private DataSecurity dataSecurity;

    @Mock
    private AdvancedCommonDao commonGraphQLDao;

    @Mock
    private ModelNames modelNames;

    private AbstractQueryResolver resolver;

    private PagingAndSortingRequest pagingAndSortingRequest;

    @BeforeEach
    void setUp() {
        pagingAndSortingRequest = new PagingAndSortingRequest();
        pagingAndSortingRequest.setLimit(2);
        pagingAndSortingRequest.setOffset(1);
        resolver = new AbstractQueryResolver(modelNames, dataSecurity, commonGraphQLDao) {
        };
        lenient().when(modelNames.determineModelNameByCollectionName("testModels")).thenReturn("TestModel");
        List<BasicModel<?>> testModels = Arrays.asList(new TestModel(), new TestModel());
        PageImpl<BasicModel<?>> objects = new PageImpl<>(testModels, pagingAndSortingRequest.getPageable(), 2);
        lenient().when(commonGraphQLDao.findAll("TestModel", pagingAndSortingRequest)).thenReturn(objects);
        lenient().when(commonGraphQLDao.get("does-not-matter")).thenReturn(new TestModel());

    }

    @Test
    @DisplayName("Returns existent model page")
    void returns_existent_model_page() {
        DefaultPaginatedDataConnection<BasicModel<?>> testModels = resolver.page(pagingAndSortingRequest, "testModels");
        verify(dataSecurity, times(1)).checkGetAllowed(any());
        assertEquals("0", testModels.getPageInfo().getStartCursor().getValue());
        assertEquals("1", testModels.getPageInfo().getEndCursor().getValue());

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> resolver.page(pagingAndSortingRequest, "unexisting_collection"));
    }

    @Test
    @DisplayName("Returns one model instance by id")
    void returns_one_model_instance_by_id() {
        Model one = resolver.one("does-not-matter");
        verify(commonGraphQLDao, times(1)).get("does-not-matter");
        verify(dataSecurity, times(1)).checkGetAllowed(any());

        assertTrue(one instanceof TestModel);
    }

    private static class TestModel implements BasicModel<UUID> {

        @Override
        public Descriptor getUuid() {
            return null;
        }

        @Override
        public void setUuid(Descriptor uuid) {

        }

        @Override
        public String getIri() {
            return null;
        }

        @Override
        public void setIri(String iri) {

        }

        @Override
        public UUID getId() {
            return null;
        }

        @Override
        public void setId(UUID uuid) {

        }
    }
}