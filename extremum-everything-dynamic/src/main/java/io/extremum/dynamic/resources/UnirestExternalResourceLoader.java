package io.extremum.dynamic.resources;

import io.extremum.dynamic.resources.exceptions.AccessForbiddenResourceLoadingException;
import io.extremum.dynamic.resources.exceptions.ResourceLoadingException;
import io.extremum.dynamic.resources.exceptions.ResourceLoadingTimeoutException;
import io.extremum.dynamic.resources.exceptions.ResourceNotFoundException;
import kong.unirest.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URI;

import static java.lang.String.format;

@Slf4j
public class UnirestExternalResourceLoader implements ExternalResourceLoader {
    private final UnirestInstance unirest;

    public UnirestExternalResourceLoader() {
        unirest = Unirest.spawnInstance();
    }

    public UnirestExternalResourceLoader(Config config) {
        this.unirest = new UnirestInstance(config);
    }

    public InputStream loadAsInputStream(URI uri, HttpMethod method) throws ResourceLoadingException {
        log.debug("Trying to load resource {} using {} method", uri, method);

        HttpRequestWithBody request = unirest.request(method.name(), uri.toString());

        try {
            HttpResponse<byte[]> response = request.asBytes();

            if (response.isSuccess()) {
                return new ByteArrayInputStream(response.getBody());
            } else {
                if (HttpStatus.NOT_FOUND.value() == response.getStatus()) {
                    log.error("Resource wasn't found {}", uri);
                    throw new ResourceNotFoundException(uri);
                } else if (HttpStatus.UNAUTHORIZED.value() == response.getStatus()) {
                    log.error("Resource wasn't found {}. Access is denied", uri);
                    throw new AccessForbiddenResourceLoadingException(uri);
                } else {
                    log.error("Unable to load resource {}: {}", uri, response.getStatus());
                    throw new ResourceLoadingException("Unable to load resource " + uri + ": " + response.getStatusText(), uri);
                }
            }
        } catch (UnirestException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                String msg = format("Unable to load resource %s, socket timeout", uri);
                log.error(msg, e);

                throw new ResourceLoadingTimeoutException(uri, e);
            } else {
                log.error("Unable to load resource {}", uri, e);
                throw new ResourceLoadingException("Unable to load resource " + uri, uri, e);
            }
        }
    }

    @Override
    public InputStream loadAsInputStream(URI uri) throws ResourceLoadingException {
        return loadAsInputStream(uri, HttpMethod.GET);
    }
}
