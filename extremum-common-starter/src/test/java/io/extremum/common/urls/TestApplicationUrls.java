package io.extremum.common.urls;

/**
 * @author rpuch
 */
public class TestApplicationUrls implements ApplicationUrls {
    @Override
    public String createExternalUrl(String applicationUri) {
        return "https://example.com" + applicationUri;
    }
}
