package io.extremum.sku.model;

import lombok.Data;

import java.util.UUID;

@Data
public class SkuMetric {
    String id;
    long timestamp;
    long sku;
    long amount;
    String appId;

    public SkuMetric(long sku, long amount, String appId) {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.sku = sku;
        this.amount = amount;
        this.appId = appId;
    }
}
