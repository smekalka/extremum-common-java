package io.extremum.sku.model;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public enum SkuID {
    UNSPECIFIED(-1),
    DATA_ENTITIES_VOLUME(0x01010100),
    DATA_ENTITIES_OPERATIONS_READ(0x01010201),
    DATA_ENTITIES_OPERATIONS_WRITE(0x01010102);

    @Getter
    private final long value;

    SkuID(long value) {
        this.value = value;
    }

    private static final Map<Long, SkuID> valueToSkuIDMap = new HashMap<>();

    static {
        for (SkuID skuId : SkuID.values()) {
            valueToSkuIDMap.put(skuId.getValue(), skuId);
        }
    }

    public static SkuID fromValue(long value) {
        return valueToSkuIDMap.get(value);
    }

    @Override
    public String toString() {
        return this.name() + "(" + "0x" + Long.toHexString(this.value) + ")";
    }
}
