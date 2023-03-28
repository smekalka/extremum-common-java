package io.extremum.sku.model;

import org.springframework.messaging.support.GenericMessage;

public class SkuMetricMessage extends GenericMessage<SkuMetric> {
    public SkuMetricMessage(SkuMetric payload) {
        super(payload);
    }
}
