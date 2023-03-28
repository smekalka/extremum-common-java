package io.extremum.common.logging;

import org.slf4j.Logger;

public class InternalErrorLogger {
    private final Logger logger;

    public InternalErrorLogger(Logger logger) {
        this.logger = logger;
    }

    public String logErrorAndReturnId(Throwable cause) {
        String errorId = generateErrorId();
        logger.error("Internal error, id '{}'", errorId, cause);
        return publicErrorMessage(errorId);
    }

    private String publicErrorMessage(String errorId) {
        return "Internal error " + errorId;
    }

    private String generateErrorId() {
        return ErrorIdGenerator.newErrorId();
    }

    public String logErrorAndReturnId(String message, Throwable cause) {
        String errorId = generateErrorId();
        logger.error("Internal error '{}', id '{}'", message, errorId, cause);
        return publicErrorMessage(errorId);
    }
}
