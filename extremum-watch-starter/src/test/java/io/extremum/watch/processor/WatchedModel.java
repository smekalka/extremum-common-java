package io.extremum.watch.processor;

import io.extremum.common.annotation.ModelName;
import io.extremum.watch.annotation.CapturedModel;
import lombok.Getter;
import lombok.Setter;

/**
 * @author rpuch
 */
@Getter
@Setter
@CapturedModel
@ModelName(ProcessorTests.WATCHED_MODEL_NAME)
class WatchedModel extends FilledModel {
    private String name;

    WatchedModel() {
        setUuid(ProcessorTests.descriptor(getId(), ProcessorTests.WATCHED_MODEL_NAME));
    }
}
