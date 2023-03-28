package io.extremum.graphql.dao.jpa;

import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.common.support.ModelClasses;
import io.extremum.everything.services.management.ModelRetriever;
import io.extremum.everything.services.management.ModelSaver;
import io.extremum.graphql.model.PagingAndSortingRequest;
import io.extremum.graphql.model.SortOrder;
import io.extremum.security.rules.service.SpecFacilities;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.Data;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import javax.persistence.Query;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaAdvancedCommonDaoTest {

    @Mock
    private ModelRetriever modelRetriever;

    @Mock
    private ModelSaver modelSaver;
    @Mock
    private ModelClasses modelClasses;
    @Mock
    private DefaultQueryBuilder queryBuilder;
    @Mock
    private Query query;

    private JpaAdvancedCommonDao dao;

    @BeforeEach
    void setUp() {
        dao = new JpaAdvancedCommonDao(modelRetriever, modelSaver, modelClasses, queryBuilder, null, new SpecFacilities(Locale.getDefault()));
    }

    @AfterEach
    public void reset_mocks() {
        Mockito.reset(modelSaver);
        Mockito.reset(queryBuilder);
    }

    @Test
    @DisplayName("Get nested string collection")
    void get_nested_string_collection() {
        PagingAndSortingRequest paging = new PagingAndSortingRequest();
        when(queryBuilder.composeQueryForCollectionIds(any(), any(), any(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(Arrays.asList(new Object[]{"value1", ""}, new Object[]{"value2", ""}));
        List<String> nested = dao.getNestedCollection(new Folder(), String.class, "nested", paging);
        Assertions.assertEquals(Arrays.asList("value1", "value2"), nested);
    }

    @Test
    @DisplayName("Get when nested collection is elements collection by is not strings collection")
    void get_nested_elements_collection() {
        PagingAndSortingRequest paging = new PagingAndSortingRequest();
        when(queryBuilder.composeQueryForCollectionIds(any(), any(), any(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(Arrays.asList(new Object[]{1, ""}, new Object[]{2, ""}));
        List<Integer> nested = dao.getNestedCollection(new Folder(), Integer.class, "nested", paging);
        Assertions.assertEquals(Arrays.asList(1, 2), nested);
    }

    @Test
    @DisplayName("Get nested models collection")
    void get_nested_models_collection() {
        PagingAndSortingRequest paging = new PagingAndSortingRequest();
        paging.setOrders(Collections.singletonList(new SortOrder(Sort.Direction.DESC, "field")));

        paging.setOrders(Collections.singletonList(new SortOrder(Sort.Direction.DESC, "field")));
        when(queryBuilder.composeQueryForCollectionIds(any(), any(), any(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(Arrays.asList(new Object[]{"eba1d8e0-fb67-4f9d-9024-9e7f00b77a76", ""}, new Object[]{"eba1d8e0-fb67-4f9d-9024-9e7f00b77a76", ""}));
        List<Model> retrieved = Arrays.asList(new Nested(), new Nested());
        when(modelRetriever.retrieveModelByIds(any())).thenReturn(retrieved);
        List<Nested> nested = dao.getNestedCollection(new Folder(), Nested.class, "nested", paging);
        Assertions.assertEquals(nested, retrieved);
    }


    @Test
    @DisplayName("Add to nested element collection")
    void add_to_nested_element_collection() {
        Folder folder = new Folder();
        when(queryBuilder.insertToNestedElementCollectionQuery(folder, "nested", 1)).thenReturn(query);
        when(queryBuilder.insertToNestedElementCollectionQuery(folder, "nested", 2)).thenReturn(query);
        dao.addToNestedCollection(folder, Arrays.asList(1, 2), "nested");

    }

    @Test
    @DisplayName("Add to nested models collection")
    void add_to_nested_model_collection() {
        Folder folder = new Folder();
        Nested nested = new Nested();
        nested.setUuid(Descriptor.builder().internalId("does-not-matter").build());
        lenient().when(modelSaver.saveModel(nested)).thenReturn(nested);
        when(queryBuilder.insertToNestedModelCollectionQuery(any(), any(), any())).thenReturn(query);

        dao.addToNestedCollection(folder, Collections.singletonList(nested), "nested");
    }

    @Test
    @DisplayName("Add to nested models collection when uuid specified")
    void add_to_nested_object_collection_when_uuid_specified() {
        Folder folder = new Folder();
        Nested nested = new Nested();

        lenient().when(modelSaver.saveModel(nested)).thenReturn(nested);
        when(queryBuilder.insertToNestedModelCollectionQuery(folder, "nested", nested.getUuid())).thenReturn(query);

        dao.addToNestedCollection(folder, Collections.singletonList(nested), "nested");
    }


    @Test
    @DisplayName("Remove from nested element collection")
    void remove_from_nested_element_collection() {
        Folder folder = new Folder();
        when(queryBuilder.deleteNestedElementCollectionQuery("nested", folder, 1)).thenReturn(query);
        when(queryBuilder.deleteNestedElementCollectionQuery("nested", folder, 2)).thenReturn(query);
        dao.removeFromNestedCollection(folder, Arrays.asList(1, 2), "nested");
    }


    @Test
    @DisplayName("Remove from nested model collection")
    void remove_from_nested_model_collection() {
        Folder folder = new Folder();
        List<Nested> nesteds = Arrays.asList(new Nested(), new Nested());
        when(queryBuilder.deleteNestedModelCollectionQuery("nested", folder, nesteds.get(0))).thenReturn(query);
        when(queryBuilder.deleteNestedModelCollectionQuery("nested", folder, nesteds.get(1))).thenReturn(query);
        dao.removeFromNestedCollection(folder, nesteds, "nested");
    }

    @Test
    void findAll() {
        PagingAndSortingRequest paging = new PagingAndSortingRequest();
        paging.setOrders(Collections.singletonList(new SortOrder(Sort.Direction.DESC, "field")));
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(new Sort.Order(Sort.Direction.DESC, "field")));
        Class<?> modelClass = Folder.class;
        when(modelClasses.getClassByModelName("TestModel")).thenReturn((Class<Model>) modelClass);
        when(modelRetriever.retrieveModelPage("TestModel", pageRequest)).thenReturn(new PageImpl<>(Collections.emptyList()));

        dao.findAll("TestModel", paging);
        verify(modelRetriever, times(1)).retrieveModelPage("TestModel", pageRequest);
    }

    @Test
    void get() {
        dao.get("does-not-matter");
        verify(modelRetriever, times(1)).retrieveModel(any());
    }

    @Data
    private static class Folder implements BasicModel<UUID> {
        private String field;
        private List<Nested> nested;
        private Descriptor uuid = Descriptor.builder().internalId("does-not-matter").build();

        @Override
        public UUID getId() {
            return null;
        }

        @Override
        public void setId(UUID uuid) {

        }

        @Override
        public Descriptor getUuid() {
            return uuid;
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

    private static class Nested implements BasicModel<UUID> {
        private String field;
        private Descriptor uuid;

        @Override
        public UUID getId() {
            return null;
        }

        @Override
        public void setId(UUID uuid) {

        }

        @Override
        public Descriptor getUuid() {
            return uuid;
        }

        @Override
        public void setUuid(Descriptor uuid) {
            this.uuid = uuid;

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