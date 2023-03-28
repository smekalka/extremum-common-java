package io.extremum.sharedmodels.descriptor;

public interface StorageType {
    String getStorageValue();

    default boolean matches(String storageValue) {
        return getStorageValue().equals(storageValue);
    }
}
