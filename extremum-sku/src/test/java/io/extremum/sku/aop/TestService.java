package io.extremum.sku.aop;

import io.extremum.sku.aop.annotation.SkuMetric;
import io.extremum.sku.model.SkuID;

public class TestService {

    @SkuMetric(sku = SkuID.DATA_ENTITIES_OPERATIONS_READ)
    public void defaultAmount() {
    }

    @SkuMetric(sku = SkuID.DATA_ENTITIES_OPERATIONS_WRITE, amount = 2)
    public void amountFromAnnotation() {
    }

    @SkuMetric(sku = SkuID.DATA_ENTITIES_OPERATIONS_WRITE, returnValue = true)
    public int returnNumericAmount() {
        return 10;
    }

    @SkuMetric(sku = SkuID.UNSPECIFIED, returnValue = true, custom = 100)
    public int returnNumericAmount_() {
        return 10;
    }

    @SkuMetric(sku = SkuID.DATA_ENTITIES_OPERATIONS_WRITE, returnValue = true)
    public String returnNonNumericAmount() {
        return "123";
    }

    @SkuMetric(sku = SkuID.DATA_ENTITIES_OPERATIONS_WRITE, returnValue = true, channel = "customChannel")
    public int sendToCustomChannelReturnedValue() {
        return 123;
    }
}
