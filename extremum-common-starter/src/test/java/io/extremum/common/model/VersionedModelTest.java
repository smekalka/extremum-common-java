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

class VersionedModelTest {
    private final Descriptor descriptor = Descriptor.builder()
            .externalId(UUID.randomUUID().toString())
            .build();
    private final TestVersionedModel from = new TestVersionedModel() {{
        setUuid(descriptor);
        setId(UUID.randomUUID());
        setCreated(ZonedDateTime.now());
        setStart(ZonedDateTime.now());
        setEnd(ZonedDateTime.now());
        setVersion(1L);
        setDeleted(true);
    }};
    private final TestVersionedModel to = new TestVersionedModel();

    @Test
    void testCopyServiceFieldsTo() {
        from.copyServiceFieldsTo(to);

        assertThat(to.getId(), is(sameInstance(from.getId())));
        assertThat(to.getUuid(), is(sameInstance(from.getUuid())));
        assertThat(to.getCreated(), is(sameInstance(from.getCreated())));
        assertThat(to.getModified(), is(sameInstance(from.getModified())));
        assertThat(to.getStart(), is(sameInstance(from.getStart())));
        assertThat(to.getEnd(), is(sameInstance(from.getEnd())));
        assertThat(to.getVersion(), is(1L));
        assertThat(to.getDeleted(), is(true));
    }

    @Getter
    @Setter
    private static class TestVersionedModel implements VersionedModel<UUID> {
        private Descriptor uuid;
        private UUID id;
        private String iri;

        private ZonedDateTime created;
        private ZonedDateTime start;
        private ZonedDateTime end;
        private Long version;
        private Boolean deleted = false;
        private String createdBy;
        private String modifiedBy;
    }
}