package io.extremum.rdf.triple.service;


import io.extremum.common.descriptor.service.ReactiveDescriptorService;
import io.extremum.everything.services.management.EverythingEverythingManagementService;
import io.extremum.sharedmodels.dto.ResponseDto;
import lombok.Setter;
import lombok.SneakyThrows;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Setter
public class IriResolver {

    private final ReactiveDescriptorService reactiveDescriptorService;

    private EverythingEverythingManagementService service;

    public IriResolver(ReactiveDescriptorService reactiveDescriptorService) {
        this.reactiveDescriptorService = reactiveDescriptorService;
    }

    @SneakyThrows
    public Mono<ResponseDto> resolve(String iri) {
        String iriPath;
        try {
            URI uri = new URI(iri);
            iriPath = uri.getPath();
        } catch (Exception e) {
            iriPath = iri;
        }

        return reactiveDescriptorService.loadByIri(iriPath).map(
                descriptor -> service.get(descriptor, false)
        );
    }

    private String encodeValue(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    }
}