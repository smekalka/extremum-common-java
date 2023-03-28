package io.extremum.test.junit5;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * @author rpuch
 */
@Slf4j
public class LoggingExtension implements BeforeEachCallback, AfterEachCallback {
    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        log.info("Going to run {}.{}", parentDisplayName(context), context.getDisplayName());
    }

    @NotNull
    private String parentDisplayName(ExtensionContext context) {
        return context.getParent().map(ExtensionContext::getDisplayName).orElse("?");
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        log.info("Finished running {}.{}", parentDisplayName(context), context.getDisplayName());
    }
}
