package io.extremum.common.collection.conversion;

import io.extremum.common.urls.ApplicationUrls;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CollectionUrlsInRoot implements CollectionUrls {
    private final ApplicationUrls applicationUrls;

    @Override
    public String collectionUrl(String collectionDescriptorExternalId) {
        return applicationUrls.createExternalUrl("/" + collectionDescriptorExternalId);
    }
}
