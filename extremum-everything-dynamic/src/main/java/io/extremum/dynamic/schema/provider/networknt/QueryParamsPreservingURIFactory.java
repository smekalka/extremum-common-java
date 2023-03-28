package io.extremum.dynamic.schema.provider.networknt;

import com.networknt.schema.uri.URIFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class QueryParamsPreservingURIFactory implements URIFactory {
    public static final Set<String> SUPPORTED_SCHEMES = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList("http", "https")));

    @Override
    public URI create(String uri) {
        try {
            return new URL(uri).toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new IllegalArgumentException("Unable to create URI", e);
        }
    }

    @Override
    public URI create(URI baseURI, String segment) {
        String baseURIQuery = baseURI.getQuery();

        try {
            if (baseURIQuery != null) {
                if (segment.contains("?")) {
                    return new URL(baseURI.toURL(), segment + "&" + baseURIQuery).toURI();
                } else {
                    return new URL(baseURI.toURL(), segment + "?" + baseURIQuery).toURI();
                }
            } else {
                return new URL(baseURI.toURL(), segment).toURI();
            }
        } catch (URISyntaxException | MalformedURLException e) {
            throw new IllegalArgumentException("Unable to create URI", e);
        }
    }
}
