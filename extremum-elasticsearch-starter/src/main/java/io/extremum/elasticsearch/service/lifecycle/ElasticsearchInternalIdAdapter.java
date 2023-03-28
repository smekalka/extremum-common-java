package io.extremum.elasticsearch.service.lifecycle;

import io.extremum.common.lifecycle.InternalIdAdapter;
import io.extremum.elasticsearch.model.ElasticsearchCommonModel;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * @author rpuch
 */
public class ElasticsearchInternalIdAdapter implements InternalIdAdapter<ElasticsearchCommonModel> {
    @Override
    public Optional<String> getInternalId(ElasticsearchCommonModel model) {
        return Optional.ofNullable(model.getId());
    }

    @Override
    public void setInternalId(ElasticsearchCommonModel model, String internalId) {
        Objects.requireNonNull(internalId, "internalId must not be null");

        model.setId(internalId);
    }

    @Override
    public String generateNewInternalId() {
        return UUID.randomUUID().toString();
    }
}
