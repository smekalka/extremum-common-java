package io.extremum.everything.services.management;

import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.common.model.CollectionFilter;
import io.extremum.common.modelservices.ModelServices;
import io.extremum.everything.services.GetterService;
import io.extremum.everything.services.ReactiveGetterService;
import io.extremum.everything.services.defaultservices.DefaultGetter;
import io.extremum.everything.services.defaultservices.DefaultReactiveGetter;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.DescriptorNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class ModelRetriever {
    private final List<GetterService<?>> getterServices;
    private final List<ReactiveGetterService<?>> reactiveGetterServices;
    private final DefaultGetter defaultGetter;
    private final DefaultReactiveGetter defaultReactiveGetter;
    private final ModelNames modelNames;

    public Model retrieveModel(Descriptor id) {
        try {
            Getter getter = findGetter(id);
            Model model = getter.get(id.getInternalId());

            logModel(id, getter, model);

            return model;
        } catch (DescriptorNotFoundException exception) {
            log.error("Model with id {} was not found", id);
            throw new ModelNotFoundException("Model was not found");
        }
    }

    public List<Model> retrieveModelByIds(List<Descriptor> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        Getter getter = findGetter(ids.get(0));

        return getter.getAllByIds(ids.stream().map(Descriptor::getInternalId).collect(Collectors.toList()));
    }

    public Page<Model> retrieveModelPage(String modelName, Pageable pageable) {
        Getter getter = findGetter(modelName);
        return getter.getAll(modelName, pageable);
    }

    public Page<Model> retrieveModelPage(String modelName, CollectionFilter filter, Pageable pageable) {
        Getter getter = findGetter(modelName);
        return getter.getAll(modelName, filter, pageable);
    }

    private Getter findGetter(Descriptor id) {
        String modelName = modelNames.determineModelName(id);

        return findGetter(modelName);
    }

    private Getter findGetter(String modelName) {
        GetterService<? extends Model> service = ModelServices.findServiceForModel(modelName, getterServices);
        if (service != null) {
            @SuppressWarnings("unchecked") GetterService<Model> castService = (GetterService<Model>) service;
            return new NonDefaultGetter(castService);
        }

        return defaultGetter;
    }

    private void logModel(Descriptor id, Object getter, Model model) {
        if (log.isDebugEnabled()) {
            if (model != null) {
                log.debug("Model with ID '{}' was found by service '{}': '{}'", id, getter, model);
            } else {
                log.debug("Model with ID '{}' wasn't found by service '{}'", id, getter);
            }
        }
    }

    public Mono<Model> retrieveModelReactively(Descriptor id) {
        ReactiveGetter getter = findReactiveGetter(id);
        return id.getInternalIdReactively()
                .flatMap(getter::get)
                .doOnNext(model -> logModel(id, getter, model));
    }

    private ReactiveGetter findReactiveGetter(Descriptor id) {
        String modelName = modelNames.determineModelName(id);
        ReactiveGetterService<? extends Model> service = ModelServices.findServiceForModel(modelName,
                reactiveGetterServices);
        if (service != null) {
            @SuppressWarnings("unchecked") ReactiveGetterService<Model> castService = (ReactiveGetterService<Model>) service;
            return new NonDefaultReactiveGetter(castService);
        }

        return defaultReactiveGetter;
    }
}
