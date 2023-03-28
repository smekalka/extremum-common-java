package io.extremum.everything.reactive.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import io.extremum.common.descriptor.service.ReactiveDescriptorService;
import io.extremum.everything.aop.ConvertNullDescriptorToModelNotFound;
import io.extremum.everything.collection.Projection;
import io.extremum.everything.controllers.CollectionStreamer;
import io.extremum.everything.controllers.EverythingControllers;
import io.extremum.everything.controllers.EverythingEverythingRestController;
import io.extremum.everything.controllers.EverythingExceptionHandlerTarget;
import io.extremum.everything.services.management.EverythingCollectionManagementService;
import io.extremum.everything.services.management.ReactiveEverythingManagementService;
import io.extremum.everything.services.management.ReactiveGetDemultiplexer;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.Response;
import io.extremum.sharedmodels.dto.ResponseDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Api(value = "Everything Everything accessor")
@Slf4j
@RestController
@EverythingExceptionHandlerTarget
@ConvertNullDescriptorToModelNotFound
public class ReactiveEverythingEverythingRestController implements EverythingEverythingRestController {
    private final ReactiveEverythingManagementService evrEvrManagementService;
    private final ReactiveGetDemultiplexer demultiplexer;

    private final CollectionStreamer collectionStreamer;
    private final ReactiveDescriptorService reactiveDescriptorService;

    public ReactiveEverythingEverythingRestController(ReactiveEverythingManagementService evrEvrManagementService,
                                                      EverythingCollectionManagementService collectionManagementService,
                                                      ReactiveGetDemultiplexer demultiplexer, ReactiveDescriptorService reactiveDescriptorService) {
        this.evrEvrManagementService = evrEvrManagementService;
        this.demultiplexer = demultiplexer;
        this.reactiveDescriptorService = reactiveDescriptorService;

        collectionStreamer = new CollectionStreamer(collectionManagementService);
    }

    @ApiOperation(value = "Everything get")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "ID of an object", required = true, example = "ef767667-29f6-457e-b90a-0d14c7fab08a"),
            @ApiImplicitParam(name = "expand", value = "Return expanded object or no", example = "false"),
            @ApiImplicitParam(name = "limit", value = "Limit for a list in a result", example = "5"),
            @ApiImplicitParam(name = "offset", value = "Page of a result list", example = "5"),
            @ApiImplicitParam(name = "since", value = "Date in format uuuu-MM-dd'T'HH:mm:ss.SSSSSSZ", example = "2018-09-26T06:47:01.000580-0500"),
            @ApiImplicitParam(name = "until", value = "Date in format uuuu-MM-dd'T'HH:mm:ss.SSSSSSZ", example = "2019-09-26T06:47:01.000580-0500"),
    })
    @GetMapping(path = EverythingControllers.EVERYTHING_UUID_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Response> get(@PathVariable String id, Projection projection,
                              @RequestParam(defaultValue = "false") boolean expand) {
        return demultiplexer.get(id, projection, expand)
                .switchIfEmpty(Mono.just(notFound()));
    }

    private Response notFound() {
        return Response.builder()
                .withFailStatus(HttpStatus.NOT_FOUND.value())
                .build();
    }

    @ApiOperation(value = "Everything patch")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "ID of an object", required = true, example = "ef767667-29f6-457e-b90a-0d14c7fab08a"),
            @ApiImplicitParam(name = "expand", value = "Return expanded object or no", example = "false"),
            @ApiImplicitParam(name = "patch", value = "Json-patch query for patching an object by id", required = true,
                    example = "[{ \"op\": \"replace\", \"path\": \"/baz\", \"value\": \"boo\" },\n" +
                            "{ \"op\": \"add\", \"path\": \"/hello\", \"value\": [\"world\"] },\n" +
                            "{ \"op\": \"remove\", \"path\": \"/foo\" }]")
    })
    @PatchMapping(path = EverythingControllers.EVERYTHING_UUID_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Response> patch(@PathVariable Descriptor id, @RequestBody JsonPatch patch,
                                @RequestParam(defaultValue = "false") boolean expand) {
        return evrEvrManagementService.patch(id, patch, expand)
                .map(Response::ok);
    }

    @ApiOperation(value = "Everything remove")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "ID of an object", required = true, example = "ef767667-29f6-457e-b90a-0d14c7fab08a")
    })
    @DeleteMapping(path = EverythingControllers.EVERYTHING_UUID_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Response> remove(@PathVariable Descriptor id) {
        return evrEvrManagementService.remove(id)
                .then(Mono.fromCallable(Response::ok));
    }

    @ApiOperation(value = "Stream collections")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "ID of an object", required = true, example = "ef767667-29f6-457e-b90a-0d14c7fab08a"),
            @ApiImplicitParam(name = "expand", value = "Return expanded object or no", example = "false"),
            @ApiImplicitParam(name = "limit", value = "Limit for a list in a result", example = "5"),
            @ApiImplicitParam(name = "offset", value = "Page of a result list", example = "5"),
            @ApiImplicitParam(name = "since", value = "Date in format uuuu-MM-dd'T'HH:mm:ss.SSSSSSZ", example = "2018-09-26T06:47:01.000580-0500"),
            @ApiImplicitParam(name = "until", value = "Date in format uuuu-MM-dd'T'HH:mm:ss.SSSSSSZ", example = "2019-09-26T06:47:01.000580-0500"),
    })
    @GetMapping(path = EverythingControllers.EVERYTHING_UUID_PATH, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Object>> streamCollection(@PathVariable String id, Projection projection,
                                                          @RequestParam(defaultValue = "false") boolean expand) {
        return collectionStreamer.streamCollection(id, projection, expand);
    }

    @GetMapping("/**")
    public Mono<Response> get(ServerHttpRequest request) {
        System.out.println("Received request" + request.getPath().value());
        return reactiveDescriptorService.loadByIri(request.getPath().value()).flatMap(
                id -> demultiplexer.get(id.getExternalId(), null, false)
                        .switchIfEmpty(Mono.just(notFound()))
        ).switchIfEmpty(Mono.just(notFound()));
    }

    @DeleteMapping("/**")
    public Mono<Response> remove(ServerHttpRequest request) {
        return reactiveDescriptorService.loadByIri(request.getPath().value()).flatMap(
                id -> evrEvrManagementService.remove(id)
                        .then(Mono.fromCallable(Response::ok))
        ).switchIfEmpty(Mono.just(notFound()));
    }

    private ObjectMapper objectMapper = new ObjectMapper();

    @PatchMapping("/**")
    public Mono<Response> patch(ServerHttpRequest request) {
        return reactiveDescriptorService.loadByIri(request.getPath().value()).flatMap(
                descriptor -> {
                    Mono<ResponseDto> responseDtoFlux = request.getBody().flatMap(
                            dataBuffer -> {
                                StringBuilder sb = new StringBuilder();

                                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(bytes);
                                DataBufferUtils.release(dataBuffer);
                                String bodyString = new String(bytes, StandardCharsets.UTF_8);
                                sb.append(bodyString);
                                Mono<ResponseDto> patch = null;
                                try {
                                    patch = evrEvrManagementService.patch(descriptor, objectMapper.readValue(sb.toString(), JsonPatch.class), true);
                                } catch (JsonProcessingException e) {
                                    e.printStackTrace();
                                }

                                return patch;
                            }).next();

                    return responseDtoFlux.map(Response::ok);
                }
        ).switchIfEmpty(Mono.just(notFound()));
    }
}