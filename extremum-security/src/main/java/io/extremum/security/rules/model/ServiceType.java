package io.extremum.security.rules.model;

public enum ServiceType {
    DB(1),
    STORAGE(2),
    FUNCTIONS(3),
    MESSAGING(4),
    CACHE(5),
    MANAGEMENT(10),
    IAM(11),
    SIGNALS(12);

    private final int value;

    ServiceType(int value) {
        this.value = value;
    }
    }