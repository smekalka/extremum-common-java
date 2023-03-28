package io.extremum.common.uuid;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author rpuch
 */
class StandardUUIDGeneratorTest {
    private final UUIDGenerator uuidGenerator = new StandardUUIDGenerator();

    @Test
    void whenUUIDIsGenerated_itShouldHaveLength36() {
        String uuid = uuidGenerator.generateUUID();

        assertThat(uuid, is(notNullValue()));
        assertThat(uuid.length(), is(36));
    }

    @Test
    void when2UUIDsAreGenerated_thenTheyShouldBeDifferent() {
        String uuid1 = uuidGenerator.generateUUID();
        String uuid2 = uuidGenerator.generateUUID();

        assertThat(uuid1, is(not(uuid2)));
    }
}