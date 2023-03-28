package io.extremum.dynamic.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class DynamicModelRegisteredEvent extends ApplicationEvent {
    private final String modelName;

    public DynamicModelRegisteredEvent(String modelName) {
        super(modelName);
        this.modelName = modelName;
    }
}
