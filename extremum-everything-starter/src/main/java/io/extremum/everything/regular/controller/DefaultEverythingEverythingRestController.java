package io.extremum.everything.regular.controller;


import com.github.fge.jsonpatch.JsonPatch;
import io.extremum.common.exceptions.UnsupportedOperationException;
import io.extremum.common.model.CollectionFilter;
import io.extremum.everything.aop.ConvertNullDescriptorToModelNotFound;
import io.extremum.everything.collection.Projection;
import io.extremum.everything.controllers.CollectionStreamer;
import io.extremum.everything.controllers.EverythingControllers;
import io.extremum.everything.controllers.EverythingEverythingRestController;
import io.extremum.everything.controllers.EverythingExceptionHandlerTarget;
import io.extremum.everything.services.management.EverythingCollectionManagementService;
import io.extremum.everything.services.management.EverythingEverythingManagementService;
import io.extremum.everything.services.management.EverythingGetDemultiplexer;
import io.extremum.sharedmodels.basic.MultilingualLanguage;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.Response;
import io.extremum.sharedmodels.dto.UniversalRequestDto;
import io.extremum.sku.aop.annotation.SkuMetric;
import io.extremum.sku.model.SkuID;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Api(tags = "Everything Everything")
@Slf4j
@CrossOrigin
@RestController
@EverythingExceptionHandlerTarget
@ConvertNullDescriptorToModelNotFound
public class DefaultEverythingEverythingRestController implements EverythingEverythingRestController {
    private final EverythingEverythingManagementService evrEvrManagementService;
    private final EverythingGetDemultiplexer demultiplexer;

    private final CollectionStreamer collectionStreamer;

    public DefaultEverythingEverythingRestController(EverythingEverythingManagementService evrEvrManagementService,
                                                     EverythingCollectionManagementService collectionManagementService,
                                                     EverythingGetDemultiplexer demultiplexer) {
        this.evrEvrManagementService = evrEvrManagementService;
        this.demultiplexer = demultiplexer;

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
    @SkuMetric(sku = SkuID.DATA_ENTITIES_OPERATIONS_READ)
    public Response get(@PathVariable Descriptor id, Projection projection,
                        @RequestParam(defaultValue = "false") boolean expand) {
        return demultiplexer.get(id, projection, expand);
    }

    @ApiOperation(value = "Everything patch")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "ID of an object", required = true, example = "ef767667-29f6-457e-b90a-0d14c7fab08a"),
            @ApiImplicitParam(name = "expand", value = "Return expanded object or no", example = "false"),
            @ApiImplicitParam(name = "patch", value = "Json-patch query for patching an object by id", paramType = "body",
                    required = true, example = "[{ \"op\": \"replace\", \"path\": \"/baz\", \"value\": \"boo\" },\n" +
                    "{ \"op\": \"add\", \"path\": \"/hello\", \"value\": [\"world\"] },\n" +
                    "{ \"op\": \"remove\", \"path\": \"/foo\" }]")
    })
    @PatchMapping(path = EverythingControllers.EVERYTHING_UUID_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    @SkuMetric(sku = SkuID.DATA_ENTITIES_OPERATIONS_WRITE)
    public Response patch(@PathVariable Descriptor id, @RequestBody JsonPatch patch,
                          @RequestParam(defaultValue = "false") boolean expand) {
        Object result = evrEvrManagementService.patch(id, patch, expand);
        return Response.ok(result);
    }

    @ApiOperation(value = "Everything remove")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "ID of an object", required = true, example = "ef767667-29f6-457e-b90a-0d14c7fab08a")
    })
    @DeleteMapping(path = EverythingControllers.EVERYTHING_UUID_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    @SkuMetric(sku = SkuID.DATA_ENTITIES_OPERATIONS_WRITE)
    public Response remove(@PathVariable Descriptor id) {
        if (id.effectiveType() == Descriptor.Type.COLLECTION) {
            throw new UnsupportedOperationException("Deleting of nested collections is not possible", 400);
        }

        evrEvrManagementService.remove(id);
        return Response.ok();
    }

    @ApiOperation(value = "Everything remove")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "ID of an object", required = true, example = "ef767667-29f6-457e-b90a-0d14c7fab08a")
    })
    @DeleteMapping(path = EverythingControllers.EVERYTHING_UUID_PATH + EverythingControllers.EVERYTHING_NESTED_UUID_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    @SkuMetric(sku = SkuID.DATA_ENTITIES_OPERATIONS_WRITE)
    public Response removeFrom(@PathVariable Descriptor id, @PathVariable Descriptor nestedId) {
        if (id.effectiveType() != Descriptor.Type.COLLECTION) {
            throw new UnsupportedOperationException("Deleting of nested items is only supported for collections", 400);
        }
        evrEvrManagementService.remove(id, nestedId);

        return Response.ok();
    }

    @PostMapping(EverythingControllers.EVERYTHING_IRI_PATH)
    @SkuMetric(sku = SkuID.DATA_ENTITIES_OPERATIONS_WRITE)
    public Response create(@PathVariable String iri, @RequestBody UniversalRequestDto dto, @RequestParam(defaultValue = "false") boolean expand) {
        return Response.ok(evrEvrManagementService.create(iri, dto, expand));
    }

    @GetMapping(EverythingControllers.EVERYTHING_IRI_PATH)
    @SkuMetric(sku = SkuID.DATA_ENTITIES_OPERATIONS_READ)
    public Response get(@PathVariable String iri, @RequestParam(defaultValue = "false") boolean expand, Projection projection, @RequestParam(required = false) String filter,
                        @RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String language) {
        try {
            return demultiplexer.get(new Descriptor(UUID.fromString(iri).toString()), projection, expand);
        } catch (IllegalArgumentException exception) {
            return evrEvrManagementService.get(iri, projection, expand, new CollectionFilter(filter, MultilingualLanguage.fromString(language)));
        }
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
    @SkuMetric(sku = SkuID.DATA_ENTITIES_OPERATIONS_READ)
    public Flux<ServerSentEvent<Object>> streamCollection(@PathVariable String id, Projection projection,
                                                          @RequestParam(defaultValue = "false") boolean expand) {
        return collectionStreamer.streamCollection(id, projection, expand);
    }
}
