package io.extremum.graphql.resolver;

import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.graphql.dao.AdvancedCommonDao;
import io.extremum.graphql.model.PagingAndSortingRequest;
import io.extremum.graphql.model.relay.DefaultPaginatedDataConnection;
import io.extremum.security.DataSecurity;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AbstractGraphQLResolverTest {

    private final TestModel testModel = new TestModel(
            0, Arrays.asList(
            new TestModel(0, Collections.emptyList()),
            new TestModel(1, Collections.emptyList()),
            new TestModel(2, Collections.emptyList()),
            new TestModel(3, Collections.emptyList()),
            new TestModel(4, Collections.emptyList()),
            new TestModel(5, Collections.emptyList())
    ));
    @Mock
    private DataSecurity dataSecurity;
    @Mock
    private AdvancedCommonDao commonGraphQLDao;
    private AbstractGraphQLResolver resolver;
    private PagingAndSortingRequest pagingAndSortingRequest;

    @BeforeEach
    void setUp() {
        pagingAndSortingRequest = new PagingAndSortingRequest();
        pagingAndSortingRequest.setLimit(2);
        pagingAndSortingRequest.setOffset(1);
        resolver = new AbstractGraphQLResolver(dataSecurity, commonGraphQLDao) {
        };
    }

    @Test
    @DisplayName("DefaultPaginatedDataConnection is composed properly")
    void composes_relay_connection_properly() {
        lenient().when(commonGraphQLDao.getNestedCollection(testModel, TestModel.class, "nested", pagingAndSortingRequest))
                .thenReturn(testModel.getNested().subList(1, 3));
        DefaultPaginatedDataConnection<TestModel> nestedPage = resolver.composeConnection(
                testModel, testModel.getNested(), TestModel.class, pagingAndSortingRequest, "nested"
        );
        verify(dataSecurity, times(1)).checkGetAllowed(any());
        assertEquals("1", nestedPage.getPageInfo().getStartCursor().getValue());
        assertEquals("2", nestedPage.getPageInfo().getEndCursor().getValue());
        assertEquals(1, nestedPage.getEdges().get(0).getNode().number);
        assertEquals(2, nestedPage.getEdges().get(1).getNode().number);

        pagingAndSortingRequest.setLimit(10);
        pagingAndSortingRequest.setOffset(4);
        lenient().when(commonGraphQLDao.getNestedCollection(testModel, TestModel.class, "nested", pagingAndSortingRequest))
                .thenReturn(testModel.getNested().subList(4, 6));
        nestedPage = resolver.composeConnection(
                testModel, testModel.getNested(), TestModel.class, pagingAndSortingRequest, "nested"
        );
        assertEquals("4", nestedPage.getPageInfo().getStartCursor().getValue());
        assertEquals("5", nestedPage.getPageInfo().getEndCursor().getValue());
        assertEquals(4, nestedPage.getEdges().get(0).getNode().number);
        assertEquals(5, nestedPage.getEdges().get(1).getNode().number);
    }

    @Test
    @DisplayName("Adds to nested collection")
    void adds_to_nested_collection() {
        resolver.addToCollection(testModel, Collections.singletonList(new TestModel(10, Collections.emptyList())), "nested");
        verify(dataSecurity, times(1)).checkPatchAllowed(any());
        verify(commonGraphQLDao, times(1)).addToNestedCollection(any(), any(), any());
    }

    @Test
    @DisplayName("Removes from nested collection")
    void removes_from_nested_collection() {
        resolver.removeFromCollection(testModel, Collections.singletonList(new TestModel(10, Collections.emptyList())), "nested");
        verify(dataSecurity, times(1)).checkPatchAllowed(any());
        verify(commonGraphQLDao, times(1)).removeFromNestedCollection(any(), any(), any());
    }

    @AllArgsConstructor
    private static class TestModel implements BasicModel<UUID> {

        private int number;
        private List<TestModel> nested;

        public List<TestModel> getNested() {
            return nested;
        }

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