package io.extremum.common.uuid;

import java.util.UUID;

/**
 * @author rpuch
 */
public final class StandardUUIDGenerator implements UUIDGenerator {
    @Override
    public String generateUUID() {
        return UUID.randomUUID().toString();
    }
}
