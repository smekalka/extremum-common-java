package io.extremum.sharedmodels.descriptor;

public class StringStorageType implements StorageType {
    private final String storageValue;

    public StringStorageType(String storageValue) {
        this.storageValue = storageValue;
    }

    @Override
    public String getStorageValue() {
        return storageValue;
    }
}
