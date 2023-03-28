package io.extremum.everything.services.management;

import io.extremum.sharedmodels.basic.Model;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public interface ReactiveGetter {
    Mono<Model> get(String id);
}
