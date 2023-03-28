package io.extremum.sku.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import static org.mockito.Mockito.lenient;

@ExtendWith({MockitoExtension.class})
class PostgresDataBaseMetricsFetcherTest {

    @Mock
    EntityManager entityManager;
    @Mock
    Query query;
    private PostgresDataBaseMetricsFetcher fetcher;

    @BeforeEach
    void initMocks() {
        lenient();
        fetcher = new PostgresDataBaseMetricsFetcher("test", entityManager);
    }

    @Test
    void should_get_database_size() {
        String queryString = "SELECT pg_database_size('" + "test" + "');";
        lenient().when(entityManager.createNativeQuery(queryString)).thenReturn(query);
        lenient().when(query.getSingleResult()).thenReturn(100);
        Assertions.assertEquals(100, fetcher.getDataBaseSize());
    }
}