package io.extremum.common.collection.conversion;

import io.extremum.common.urls.TestApplicationUrls;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class CollectionUrlsInRootTest {
    private final CollectionUrlsInRoot collectionUrls = new CollectionUrlsInRoot(new TestApplicationUrls());

    @Test
    void mapsCollectionUrisToRoot() {
        String collectionUrl = collectionUrls.collectionUrl("external-id");

        assertThat(collectionUrl, is("https://example.com/external-id"));
    }
}