package io.extremum.elasticsearch.dao.extractor;

import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.common.descriptor.service.DescriptorService;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.get.GetResponse;

import java.util.Map;

@RequiredArgsConstructor
public class GetResponseAccessorFacade extends AccessorFacade {
    private final GetResponse response;
    private final DescriptorService descriptorService;

    @Override
    public String getId() {
        return response.getId();
    }

    @Override
    public Descriptor getUuid() {
        return descriptorService.loadByInternalId(response.getId())
                .orElseThrow(() -> new IllegalStateException(
                        String.format("Did not find a descriptor by id '%s'", response.getId())));
    }

    @Override
    public long getVersion() {
        return response.getVersion();
    }

    @Override
    public String getRawSource() {
        return response.getSourceAsString();
    }

    @Override
    public long getSeqNo() {
        return response.getSeqNo();
    }

    @Override
    public long getPrimaryTerm() {
        return response.getPrimaryTerm();
    }

    @Override
    public Map<String, Object> getSourceAsMap() {
        return response.getSourceAsMap();
    }
}
