package io.extremum.graphql.dao.jpa;

import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.graphql.model.PagingAndSortingRequest;
import io.extremum.graphql.model.SortOrder;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.Data;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdvancedQueryBuilderTest {

    @Spy
    private EntityManager entityManager;

    @Mock
    private Query query;

    @Test
    @DisplayName("Compose query to get nested collection identifiers")
    void compose_query_to_get_nested_collection_identifiers() {
        DefaultQueryBuilder queryBuilder = new DefaultQueryBuilder(entityManager);

        PagingAndSortingRequest request = new PagingAndSortingRequest();
        request.setOrders(Arrays.asList(new SortOrder(Sort.Direction.DESC, "property1"), new SortOrder(Sort.Direction.ASC, "property2")));
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setFirstResult(request.getOffset())).thenReturn(query);
        when(query.setMaxResults(request.getLimit())).thenReturn(query);

        queryBuilder.composeQueryForCollectionIds(new Folder(), Nested.class, "nested", request.getPageable());

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createNativeQuery(argument.capture());
        System.out.println(argument.getValue());
        assertEquals("select     cast(nested_id as varchar)  as nested_id,     cast(jt.folder_id as varchar)  as model_id from folder_nested jt         inner join nested r on nested_id = r.id where jt.folder_id=:id order by property1 DESC,property2 ASC", argument.getValue());
    }

    @Test
    @DisplayName("Compose query to get nested collection primitive values")
    void compose_query_to_get_nested_primitive_collection_values() {
        DefaultQueryBuilder queryBuilder = new DefaultQueryBuilder(entityManager);

        PagingAndSortingRequest request = new PagingAndSortingRequest();
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setFirstResult(request.getOffset())).thenReturn(query);
        when(query.setMaxResults(request.getLimit())).thenReturn(query);

        queryBuilder.composeQueryForCollectionIds(new Folder(), String.class, "strings", request.getPageable());
        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createNativeQuery(argument.capture());

        assertEquals("select\n" +
                "    cast(strings as varchar)  as nested_id,\n" +
                "    cast(folder_id as varchar)  as model_id\n" +
                "from folder_strings         inner join folder r on folder_id = r.id\n" +
                "where folder_id=:id", argument.getValue());
    }

    @Test
    void deleteNestedPrimitiveQuery() {
        DefaultQueryBuilder queryBuilder = new DefaultQueryBuilder(entityManager);

        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);

        queryBuilder.deleteNestedElementCollectionQuery("collection", new Folder(), "primitive string");

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createNativeQuery(argument.capture());
        assertEquals("delete  from folder_collection where collection=:value", argument.getValue());
    }

    @Test
    void deleteNestedModelQuery() {
        DefaultQueryBuilder queryBuilder = new DefaultQueryBuilder(entityManager);

        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);

        queryBuilder.deleteNestedModelCollectionQuery("nested", new Folder(), new Nested());

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createNativeQuery(argument.capture());
        assertEquals("delete from folder_nested where nested_id=:id", argument.getValue());

    }

    @Test
    void insertToNestedObjectCollection() {
        DefaultQueryBuilder queryBuilder = new DefaultQueryBuilder(entityManager);

        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);

        queryBuilder.insertToNestedModelCollectionQuery(new Folder(), "nested", new Nested().getUuid());

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createNativeQuery(argument.capture());
        assertEquals("insert into folder_nested values (:folder_id, :nested_id)", argument.getValue());

    }

    @Test
    void insertToNestedPrimitiveCollection() {
        DefaultQueryBuilder queryBuilder = new DefaultQueryBuilder(entityManager);

        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);

        queryBuilder.insertToNestedElementCollectionQuery(new Folder(), "nested", "string value");

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createNativeQuery(argument.capture());
        assertEquals("insert into folder_nested values (:folder_id, :value)", argument.getValue());
    }

    @Data
    public static class Folder implements BasicModel<UUID> {

        private List<Nested> nested;

        @Override
        public Descriptor getUuid() {
            return Descriptor.builder().internalId("ec0aba34-1e12-11ed-861d-0242ac120002").build();
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

    private static class Nested implements BasicModel<UUID> {
        @Override
        public UUID getId() {
            return null;
        }

        @Override
        public void setId(UUID uuid) {

        }

        @Override
        public Descriptor getUuid() {
            return Descriptor.builder()
                    .modelType("Nested")
                    .internalId("ec0aba34-1e12-11ed-861d-0242ac120002").build();
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
    }
}