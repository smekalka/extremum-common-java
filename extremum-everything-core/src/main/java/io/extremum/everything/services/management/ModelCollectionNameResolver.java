package io.extremum.everything.services.management;


public interface ModelCollectionNameResolver {

    String resolveModelName(String collectionName);

    void register(String modelName, String collectionName);
}