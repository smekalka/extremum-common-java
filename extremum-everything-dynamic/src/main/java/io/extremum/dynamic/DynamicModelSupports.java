package io.extremum.dynamic;

public class DynamicModelSupports {
    public static String collectionNameFromModel(String modelName) {
        return modelName.toLowerCase().replaceAll("[\\W]", "_");
    }
}
