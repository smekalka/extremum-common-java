package io.extremum.common.logging;

import java.util.UUID;

final class ErrorIdGenerator {
    static String newErrorId() {
        return UUID.randomUUID().toString();
    }

    private ErrorIdGenerator() {
    }
}
