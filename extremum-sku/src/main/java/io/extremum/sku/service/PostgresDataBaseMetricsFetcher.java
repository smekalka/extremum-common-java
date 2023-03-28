package io.extremum.sku.service;

import io.extremum.sku.aop.annotation.SkuMetric;
import io.extremum.sku.model.SkuID;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManager;
import javax.persistence.Query;

@Slf4j
public class PostgresDataBaseMetricsFetcher implements DataBaseMetricsFetcher {

    private final String dbName;
    private final EntityManager entityManager;
    public PostgresDataBaseMetricsFetcher(String dbName, EntityManager entityManager) {
        this.dbName = dbName;
        this.entityManager = entityManager;
    }

    @Override
    @SkuMetric(sku = SkuID.DATA_ENTITIES_VOLUME, returnValue = true)
    public long getDataBaseSize() {
        Query dbSizeQuery = entityManager.createNativeQuery("SELECT pg_database_size('" + dbName + "');");
        Object singleResult = dbSizeQuery.getSingleResult();
        if (singleResult instanceof Number) {
            return ((Number) singleResult).longValue();
        } else {
            log.error("Unable to get database size");
            return 0;
        }
    }
}
