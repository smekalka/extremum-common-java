package io.extremum.everything.services.management;

import io.extremum.everything.services.Inflector;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;

@Slf4j
public class DefaultModelCollectionNameResolver implements ModelCollectionNameResolver {

    private final Inflector inflector = Inflector.getInstance();
    private final java.util.Map<String, String> custom = new HashMap<>();

    @Override
    public String resolveModelName(String collectionName) {
       return custom.getOrDefault(collectionName, inflector.singularize(collectionName));
    }

    @Override
    public void register(String modelName, String collectionName) {
        log.info("Register custom endpoint /{} to handle model \"{}\" collection", collectionName, modelName);
        custom.put(collectionName, modelName);
    }
}