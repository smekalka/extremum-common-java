package io.extremum.common.model;

import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author rpuch
 */
class PersistableCommonModelTest {
    private final Descriptor descriptor = Descriptor.builder()
            .externalId(UUID.randomUUID().toString())
            .build();
    private final TestPersistableModel from = new TestPersistableModel() {{
        setUuid(descriptor);
        setId(UUID.randomUUID());
        setCreated(ZonedDateTime.now());
        setModified(ZonedDateTime.now());
        setVersion(1L);
        setDeleted(true);
    }};
    private final TestPersistableModel to = new TestPersistableModel();

    @Test
    void testCopyServiceFieldsTo() {
        from.copyServiceFieldsTo(to);

        assertThat(to.getId(), is(sameInstance(from.getId())));
        assertThat(to.getUuid(), is(sameInstance(from.getUuid())));
        assertThat(to.getCreated(), is(sameInstance(from.getCreated())));
        assertThat(to.getModified(), is(sameInstance(from.getModified())));
        assertThat(to.getVersion(), is(1L));
        assertThat(to.getDeleted(), is(true));
    }

    @Getter
    @Setter
    private static class TestPersistableModel implements PersistableCommonModel<UUID> {
        private Descriptor uuid;
        private UUID id;
        private String iri;

        private ZonedDateTime created;
        private ZonedDateTime modified;
        private String createdBy;
        private String modifiedBy;
        private Long version;
        private Boolean deleted = false;
    }
}