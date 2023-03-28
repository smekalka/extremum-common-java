package io.extremum.watch.processor;

import io.extremum.common.annotation.ModelName;
import lombok.Getter;
import lombok.Setter;

/**
 * @author rpuch
 */
@Getter
@Setter
@ModelName(ProcessorTests.NON_WATCHED_MODEL_NAME)
class NonWatchedModel extends FilledModel {
    private String name;

    NonWatchedModel() {
        setUuid(ProcessorTests.descriptor(getId(), ProcessorTests.NON_WATCHED_MODEL_NAME));
    }
}
