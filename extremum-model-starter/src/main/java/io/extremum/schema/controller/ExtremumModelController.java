package io.extremum.schema.controller;

import com.github.fge.jsonpatch.JsonPatch;
import io.extremum.schema.model.ModelSchema;
import io.extremum.schema.service.ExtremumModelSchemaService;
import io.extremum.sharedmodels.dto.Pagination;
import io.extremum.sharedmodels.dto.Response;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
@AllArgsConstructor
public class ExtremumModelController {

    private final ExtremumModelSchemaService modelService;

    @GetMapping("/models")
    public Response getModels(
            @RequestParam(defaultValue = "false", required = false) boolean expand,
            @RequestParam(defaultValue = "10", required = false) long limit,
            @RequestParam(defaultValue = "0", required = false) long offset,
            @RequestParam(defaultValue = "", required = false) String prefix
    ) {
        Page<ModelSchema> modelSchemasPage = modelService
                .getModels(expand, limit, offset, prefix);

        return Response.ok(
                modelSchemasPage.getContent(),
                Pagination
                        .builder()
                        .total(modelSchemasPage.getTotalElements())
                        .count(modelSchemasPage.getContent().size())
                        .build()
        );
    }

    @SneakyThrows
    @GetMapping("/models/**")
    public Response getModel(HttpServletRequest request) {
        String modelPath = URLDecoder.decode(request.getRequestURI(), StandardCharsets.UTF_8.toString())
                .split(request.getContextPath() + "/models/")[1].trim();

        return Response.ok(modelService.getModel(modelPath));
    }

    @SneakyThrows
    @RequestMapping(value = "/models/**", method = RequestMethod.PATCH)
    public Response patchModel(
            HttpServletRequest request,
            @RequestBody JsonPatch patch
    ) {
        String modelPath = URLDecoder.decode(request.getRequestURI(), StandardCharsets.UTF_8.toString())
                .split(request.getContextPath() + "/models/")[1].trim();

        return Response.ok(modelService.patchModel(modelPath, patch));
    }
}