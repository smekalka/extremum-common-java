package io.extremum.everything.support;

import io.extremum.sharedmodels.basic.Model;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public interface ReactiveModelDescriptors {
    <M extends Model> Mono<Class<M>> getModelClassByModelInternalId(String internalId);
}
