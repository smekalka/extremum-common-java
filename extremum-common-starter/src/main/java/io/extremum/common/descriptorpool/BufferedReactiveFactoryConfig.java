package io.extremum.common.descriptorpool;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(builderClassName = "Builder")
@Getter
public class BufferedReactiveFactoryConfig {
    private final int batchSize;
    private final float startAllocationThreshold;
    private final int maxClientsToWaitForAllocation;
    private final long checkForAllocationEachMillis;

    public static Builder builder() {
        return new Builder()
                .startAllocationThreshold(0.1f)
                .maxClientsToWaitForAllocation(1000)
                .checkForAllocationEachMillis(1000);
    }

    public void validate() {
        if (batchSize <= 0) {
            throwInvalidConfigException("batchSize must be positive");
        }
        if (startAllocationThreshold <= 0.0) {
            throwInvalidConfigException("startAllocationThreshold must be positive");
        }
        if (startAllocationThreshold > 1.0) {
            throwInvalidConfigException("startAllocationThreshold must not be greater than 1.0");
        }
        if (maxClientsToWaitForAllocation <= 0) {
            throwInvalidConfigException("maxClientsToWaitForAllocation must be positive");
        }
        if (checkForAllocationEachMillis <= 0) {
            throwInvalidConfigException("checkForAllocationEachMillis must be positive");
        }
    }

    private void throwInvalidConfigException(String errorMessage) {
        throw new IllegalStateException(errorMessage);
    }
}
