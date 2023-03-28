package io.extremum.dynamic.schema.provider.networknt;

import com.networknt.schema.uri.URIFactory;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class QueryParamsPreservingURIFactoryTest {
    @Test
    void create() {
        URIFactory factory = new QueryParamsPreservingURIFactory();
        String uriString = "http://localhost/path/to/resource?param=value&p2=v2";
        URI resultUri = factory.create(uriString);

        assertEquals(uriString, resultUri.toString());
    }

    @Test
    void createWithBaseUriAndSegment() {
        URIFactory factory = new QueryParamsPreservingURIFactory();

        String base = "http://localhost/path/to/";
        String pathToOtherResource = "path/to/other/resource";

        String uriString = base + "resource";

        URI resultUri = factory.create(URI.create(uriString), pathToOtherResource);

        assertEquals(base + pathToOtherResource, resultUri.toString());
    }

    @Test
    void createWithBaseUriAndSegment_segmentHaveQueryParams_baseUriHaveQueryParams() {
        URIFactory factory = new QueryParamsPreservingURIFactory();

        String p1 = "param=value";
        String p2 = "p2=v2";

        String queryParamsString = p1 + "&" + p2;
        String base = "http://localhost/path/to/";
        String oP1 = "otherParam=value";
        String pathToOtherResource = "path/to/other/resource?" + oP1;

        String uriString = base + "resource" + "?" + queryParamsString;

        URI resultUri = factory.create(URI.create(uriString), pathToOtherResource);

        String resultQueryParams = resultUri.getQuery();

        assertNotNull(resultQueryParams);

        String[] splitted = resultQueryParams.split("&");
        assertEquals(3, splitted.length);

        HashSet<String> params = new HashSet<>(Arrays.asList(splitted));
        assertTrue(params.contains(p1));
        assertTrue(params.contains(p2));
        assertTrue(params.contains(oP1));
    }

    @Test
    void createWithBaseUriAndSegment_segmentHaveQueryParams_only() {
        URIFactory factory = new QueryParamsPreservingURIFactory();

        String base = "http://localhost/path/to/";
        String oP1 = "otherParam=value";
        String pathToOtherResource = "path/to/other/resource?" + oP1;

        String uriString = base + "resource";

        URI resultUri = factory.create(URI.create(uriString), pathToOtherResource);

        String resultQueryParams = resultUri.getQuery();

        assertNotNull(resultQueryParams);

        String[] splitted = resultQueryParams.split("&");
        assertEquals(1, splitted.length);

        HashSet<String> params = new HashSet<>(Arrays.asList(splitted));
        assertTrue(params.contains(oP1));
    }

    @Test
    void createWithBaseUriAndSegment_baseUriHaveQueryParams_only() {
        URIFactory factory = new QueryParamsPreservingURIFactory();

        String p1 = "param=value";
        String p2 = "p2=v2";

        String queryParamsString = p1 + "&" + p2;
        String base = "http://localhost/path/to/";
        String pathToOtherResource = "path/to/other/resource";

        String uriString = base + "resource" + "?" + queryParamsString;

        URI resultUri = factory.create(URI.create(uriString), pathToOtherResource);

        String resultQueryParams = resultUri.getQuery();

        assertNotNull(resultQueryParams);

        String[] splitted = resultQueryParams.split("&");
        assertEquals(2, splitted.length);

        HashSet<String> params = new HashSet<>(Arrays.asList(splitted));
        assertTrue(params.contains(p1));
        assertTrue(params.contains(p2));
    }
}
