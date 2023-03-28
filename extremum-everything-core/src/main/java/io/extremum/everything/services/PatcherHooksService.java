package io.extremum.everything.services;

import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.dto.RequestDto;

/**
 * Service that allows to supply hooks to Everything-Everything PATCH operation to customize it.
 *
 * @param <M> model type
 * @param <D> request DTO type
 *
 * @author rpuch
 */
public interface PatcherHooksService<M extends Model, D extends RequestDto> extends EverythingEverythingService {
    default D afterPatchAppliedToDto(M model, D dto) {
        return dto;
    }

    default void beforeSave(PatchPersistenceContext<M> context) {
    }

    default void afterSave(PatchPersistenceContext<M> context) {
    }
}
