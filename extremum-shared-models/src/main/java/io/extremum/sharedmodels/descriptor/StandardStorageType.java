package io.extremum.sharedmodels.descriptor;

public enum StandardStorageType implements StorageType {
    MONGO("mongo"),
    ELASTICSEARCH("elastic"),
    POSTGRES("postgres");

    private final String value;

    StandardStorageType(String value) {
        this.value = value;
    }

    @Override
    public String getStorageValue() {
        return value;
    }
}
