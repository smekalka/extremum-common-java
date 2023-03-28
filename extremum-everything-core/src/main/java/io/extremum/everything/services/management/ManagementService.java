package io.extremum.everything.services.management;

import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.everything.services.EverythingEverythingService;

import java.util.Collection;
import java.util.function.Supplier;

public interface ManagementService {
    String determineModelNameById(Descriptor id);

    String determineModelNameByIdOrThrow(Descriptor id, Supplier<? extends RuntimeException> exceptionSupplier);

    <T extends EverythingEverythingService> T findServiceForModel(String modelName,
                                                                  Collection<? extends EverythingEverythingService> services,
                                                                  Class<T> type);

    <T extends EverythingEverythingService> T findServiceForModelOrElseThrow(String modelName,
                                                                             Collection<? extends EverythingEverythingService> services,
                                                                             Supplier<? extends RuntimeException> exceptionSupplier,
                                                                             Class<T> type);
}
