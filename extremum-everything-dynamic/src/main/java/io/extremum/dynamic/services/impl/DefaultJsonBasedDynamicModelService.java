package io.extremum.dynamic.services.impl;

import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.dynamic.DynamicModelSupports;
import io.extremum.dynamic.dao.JsonDynamicModelDao;
import io.extremum.dynamic.metadata.MetadataProviderService;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.dynamic.services.DateTypesNormalizer;
import io.extremum.dynamic.services.DatesProcessor;
import io.extremum.dynamic.services.JsonBasedDynamicModelService;
import io.extremum.dynamic.validator.ValidationContext;
import io.extremum.dynamic.validator.services.impl.JsonDynamicModelValidator;
import io.extremum.dynamic.watch.DynamicModelWatchService;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static reactor.core.publisher.Mono.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultJsonBasedDynamicModelService implements JsonBasedDynamicModelService {
    private final JsonDynamicModelDao dao;
    private final JsonDynamicModelValidator modelValidator;
    private final MetadataProviderService metadataProvider;
    private final DateTypesNormalizer dateTypesNormalizer;
    private final DatesProcessor datesProcessor;
    private final DynamicModelWatchService watchService;

    @Override
    public Mono<JsonDynamicModel> saveModelWithoutNotifications(JsonDynamicModel model) {
        return modelValidator.validate(model)
                .flatMap(validated ->
                        validated.fold(
                                Mono::error,
                                ctx -> saveValidatedModel(model, ctx)
                        ))
                .map(m -> {
                    datesProcessor.processDates(m.getModelData());
                    return m;
                });
    }

    @Override
    public Mono<JsonDynamicModel> saveModel(JsonDynamicModel model) {
        return saveModelWithoutNotifications(model)
                .flatMap(savedModel ->
                        watchService.registerSaveOperation(savedModel)
                                .thenReturn(savedModel));
    }

    private Mono<JsonDynamicModel> saveValidatedModel(JsonDynamicModel model, ValidationContext ctx) {
        return fromSupplier(normalize(model, ctx))
                .flatMap(findCollection(model))
                .flatMap(processWithDao());
    }

    @NotNull
    protected Function<JsonDynamicModel, JsonDynamicModel> normalize() {
        return bModel -> {
            Map<String, Object> mapWithReplacedDates = datesProcessor.processDates(bModel.getModelData());

            return new JsonDynamicModel(bModel.getId(), bModel.getModelName(), mapWithReplacedDates);
        };
    }

    private Function<Tuple2<JsonDynamicModel, String>, Mono<? extends JsonDynamicModel>> processWithDao() {
        return tuple -> {
            JsonDynamicModel bModel = tuple.getT1();
            String collectionName = tuple.getT2();

            if (isNewModel(bModel)) {
                return dao.create(bModel, collectionName);
            } else {
                return dao.update(bModel, collectionName);
            }
        };
    }

    private Function<JsonDynamicModel, Mono<? extends Tuple2<JsonDynamicModel, String>>> findCollection(JsonDynamicModel model) {
        return bModel -> getCollectionName(model).map(cName -> Tuples.of(bModel, cName));
    }

    private Supplier<JsonDynamicModel> normalize(JsonDynamicModel model, ValidationContext ctx) {
        return () -> {
            dateTypesNormalizer.normalize(model.getModelData(), ctx.getPaths());
            return model;
        };
    }

    private boolean isNewModel(JsonDynamicModel model) {
        return model.getId() == null;
    }

    @Override
    public Mono<JsonDynamicModel> findById(Descriptor id) {
        return getCollectionName(id)
                .flatMap(cName -> dao.getByIdFromCollection(id, cName))
                .map(normalize())
                .doOnNext(metadataProvider::provideMetadata)
                .switchIfEmpty(defer(() -> error(new ModelNotFoundException("DynamicModel with id " + id + " not found"))));
    }

    @Override
    public Mono<JsonDynamicModel> remove(Descriptor id) {
        return findById(id)
                .flatMap(found ->
                        getCollectionName(id)
                                .flatMap(cName -> dao.remove(id, cName)
                                        .thenReturn(found))
                                .flatMap(model -> watchService.registerDeleteOperation(model).thenReturn(model))
                );
    }

    private Mono<String> getCollectionName(Descriptor descr) {
        return descr.getModelTypeReactively().map(DynamicModelSupports::collectionNameFromModel);
    }

    private Mono<String> getCollectionName(JsonDynamicModel model) {
        if (model.getId() != null) {
            return getCollectionName(model.getId());
        } else {
            return just(model.getModelName())
                    .map(DynamicModelSupports::collectionNameFromModel);
        }
    }
}
