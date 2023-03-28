package io.extremum.everything.services.management;

import com.github.fge.jsonpatch.JsonPatch;
import io.extremum.common.descriptor.service.ReactiveDescriptorService;
import io.extremum.common.dto.converters.ConversionConfig;
import io.extremum.common.dto.converters.services.DtoConversionService;
import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.common.modelservices.ModelServices;
import io.extremum.everything.services.ReactiveRemovalService;
import io.extremum.everything.services.defaultservices.DefaultReactiveRemover;
import io.extremum.security.ReactiveDataSecurity;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class DefaultReactiveEverythingManagementService implements ReactiveEverythingManagementService {
    private final ModelRetriever modelRetriever;
    private final ReactivePatchFlow patchFlow;
    private final List<ReactiveRemovalService> removalServices;
    private final DefaultReactiveRemover defaultRemover;
    private final DtoConversionService dtoConversionService;
    private final ReactiveDataSecurity dataSecurity;
    private final ModelNames modelNames;

    @Override
    public Mono<ResponseDto> get(Descriptor id, boolean expand) {
        return modelRetriever.retrieveModelReactively(id)
                .flatMap(model -> dataSecurity.checkGetAllowed(model).thenReturn(model))
                .flatMap(model -> convertModelToResponseDto(model, expand))
                .switchIfEmpty(Mono.defer(() -> Mono.error(newModelNotFoundException(id))));
    }

    private ModelNotFoundException newModelNotFoundException(Descriptor id) {
        return new ModelNotFoundException(String.format("Nothing was found by '%s'", id.getExternalId()));
    }

    private Mono<ResponseDto> convertModelToResponseDto(Model model, boolean expand) {
        ConversionConfig conversionConfig = ConversionConfig.builder().expand(expand).build();
        return dtoConversionService.convertUnknownToResponseDtoReactively(model, conversionConfig);
    }

    @Override
    public Mono<ResponseDto> patch(Descriptor id, JsonPatch patch, boolean expand) {
        return patchFlow.patch(id, patch)
                .flatMap(patched -> convertModelToResponseDto(patched, expand));
    }

    @Override
    public Mono<Void> remove(Descriptor id) {
        return checkDataSecurityAllowsRemoval(id)
                .then(Mono.fromSupplier(() -> findRemover(id)))
                .flatMap(remover -> id.getInternalIdReactively()
                        .flatMap(remover::remove));
    }

    private ReactiveRemover findRemover(Descriptor id) {
        String modelName = modelNames.determineModelName(id);

        ReactiveRemovalService removalService = ModelServices.findServiceForModel(modelName, removalServices);
        if (removalService != null) {
            return new NonDefaultReactiveRemover(removalService);
        }

        return defaultRemover;
    }

    private Mono<Void> checkDataSecurityAllowsRemoval(Descriptor id) {
        return modelRetriever.retrieveModelReactively(id)
                .doOnNext(dataSecurity::checkRemovalAllowed)
                .then();
    }
}
