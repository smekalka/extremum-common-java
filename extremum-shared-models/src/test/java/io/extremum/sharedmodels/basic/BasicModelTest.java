package io.extremum.sharedmodels.basic;

import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author rpuch
 */
class BasicModelTest {
    private final Descriptor descriptor = Descriptor.builder()
            .externalId(UUID.randomUUID().toString())
            .build();
    private final TestBasicModel from = new TestBasicModel() {{
        setUuid(descriptor);
        setId(UUID.randomUUID());
        setIri("does-not-matter");
    }};
    private final TestBasicModel to = new TestBasicModel();

    @Test
    void testCopyServiceFieldsTo() {
        from.copyServiceFieldsTo(to);

        assertThat(to.getId(), is(sameInstance(from.getId())));
        assertThat(to.getUuid(), is(sameInstance(from.getUuid())));
        assertThat(to.getIri(), is(sameInstance(from.getIri())));
    }

    @Getter @Setter
    private static class TestBasicModel implements BasicModel<UUID> {
        private Descriptor uuid;
        private UUID id;
        private String iri;
    }
}