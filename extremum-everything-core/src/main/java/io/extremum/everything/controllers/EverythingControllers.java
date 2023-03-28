package io.extremum.everything.controllers;

public class EverythingControllers {
    public static final String EVERYTHING_UUID_PATH = "/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}";
    public static final String EVERYTHING_NESTED_UUID_PATH = "/{nestedId:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}";
    public static final String EVERYTHING_IRI_PATH = "/{iri:[A-Za-z0-9_-]+}";

    private EverythingControllers() {
    }
}
