package io.extremum.sku.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class SkuMetricTest {

    @Test
    void should_construct_sku_metric_properly() {
        long now = System.currentTimeMillis();
        SkuMetric skuMetric = new SkuMetric(SkuID.DATA_ENTITIES_VOLUME.getValue(), 123, "app-id");
        long next = System.currentTimeMillis();

        Assertions.assertEquals(skuMetric.getSku(), SkuID.DATA_ENTITIES_VOLUME.getValue());
        Assertions.assertEquals(skuMetric.getAmount(), 123);
        Assertions.assertEquals(skuMetric.getAppId(), "app-id");
        Assertions.assertTrue(skuMetric.getTimestamp() >= now && skuMetric.getTimestamp() <= next);
        UUID uuid = UUID.fromString(skuMetric.getId());
    }
}