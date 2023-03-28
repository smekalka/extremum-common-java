package io.extremum.common.modelservices;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public final class ModelServices {
    public static <T extends ModelService> T findServiceForModel(String modelName,
            Collection<? extends T> services) {
        requireNonNull(modelName, "Name of a model can't be null");
        requireNonNull(services, "Services list can't be null");

        return services.stream()
                .filter(getIsServiceSupportsModelFilter(modelName))
                .findAny()
                .orElse(null);
    }

    private static Predicate<? super ModelService> getIsServiceSupportsModelFilter(String modelName) {
        return service -> modelName.equals(service.getSupportedModel());
    }

    private ModelServices() {
    }
}
