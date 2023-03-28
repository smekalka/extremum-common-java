package io.extremum.common.urls;

import lombok.AllArgsConstructor;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@AllArgsConstructor
public class ApplicationUrlsImpl implements ApplicationUrls {

    private String appHost;

    @Override
    public String createExternalUrl(String applicationUri) {
        try {
            return ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/" + applicationUri)
                    .build()
                    .toUriString();
        } catch (IllegalStateException exception) {
            return ServletUriComponentsBuilder.fromHttpUrl(appHost)
                    .path(applicationUri)
                    .build()
                    .toUriString();
        }
    }
}
