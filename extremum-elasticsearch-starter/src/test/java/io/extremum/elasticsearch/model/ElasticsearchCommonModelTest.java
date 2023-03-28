package io.extremum.elasticsearch.model;

import io.extremum.sharedmodels.descriptor.Descriptor;
import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.core.query.SeqNoPrimaryTerm;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author rpuch
 */
class ElasticsearchCommonModelTest {
    private final Descriptor descriptor = Descriptor.builder()
            .externalId(UUID.randomUUID().toString())
            .build();
    private final TestElasticsearchModel from = new TestElasticsearchModel() {{
        setUuid(descriptor);
        setId(UUID.randomUUID().toString());

        setCreated(ZonedDateTime.now());
        setModified(ZonedDateTime.now());
        setVersion(1L);
        setDeleted(true);

        setSeqNoPrimaryTerm(new SeqNoPrimaryTerm(2, 3));
    }};
    private final TestElasticsearchModel to = new TestElasticsearchModel();

    @Test
    void testCopyServiceFieldsTo() {
        from.copyServiceFieldsTo(to);

        assertThat(to.getId(), is(sameInstance(from.getId())));
        assertThat(to.getUuid(), is(sameInstance(from.getUuid())));

        assertThat(to.getCreated(), is(sameInstance(from.getCreated())));
        assertThat(to.getModified(), is(sameInstance(from.getModified())));
        assertThat(to.getVersion(), is(1L));
        assertThat(to.getDeleted(), is(sameInstance(from.getDeleted())));

        assertThat(to.getSeqNoPrimaryTerm().getSequenceNumber(), is(2L));
        assertThat(to.getSeqNoPrimaryTerm().getPrimaryTerm(), is(3L));
    }

    private static class TestElasticsearchModel extends ElasticsearchCommonModel {
    }
}