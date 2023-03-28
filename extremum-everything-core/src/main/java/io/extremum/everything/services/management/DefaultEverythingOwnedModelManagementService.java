package io.extremum.everything.services.management;

import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.common.utils.ReflectionUtils;
import io.extremum.security.ReactiveDataSecurity;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.Response;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class DefaultEverythingOwnedModelManagementService implements EverythingOwnedModelManagementService {

    private final ModelRetriever modelRetriever;
    private final ReactiveDataSecurity dataSecurity;

    @Override
    public Mono<Response> fetchOwnedModelReactively(Descriptor id, boolean expand) {
        return modelRetriever.retrieveModelReactively(id.getOwned().getCoordinates().getOwnedCoordinates().getHostId())
                .flatMap(model -> dataSecurity.checkGetAllowed(model).thenReturn(model))
                .flatMap(model -> convertModelToResponseDto(model, id))
                .switchIfEmpty(Mono.defer(() -> Mono.error(newModelNotFoundException(id))));
    }

    private ModelNotFoundException newModelNotFoundException(Descriptor id) {
        return new ModelNotFoundException(String.format("Nothing was found by '%s'", id.getExternalId()));
    }

    private Mono<Response> convertModelToResponseDto(Model model, Descriptor id) {
        return Mono.just(Response.ok(ReflectionUtils.getFieldValue(model, id.getOwned().getCoordinates().getOwnedCoordinates().getHostAttributeName())));
    }
}
