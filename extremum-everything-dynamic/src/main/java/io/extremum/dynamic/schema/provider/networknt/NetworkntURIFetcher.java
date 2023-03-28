package io.extremum.dynamic.schema.provider.networknt;

import com.networknt.schema.uri.URIFetcher;

import java.util.List;

public interface NetworkntURIFetcher extends URIFetcher {
    List<String> getSupportedSchemas();
}
