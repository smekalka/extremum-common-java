package io.extremum.sharedmodels.watch;

import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;

public class ModelSignalMessage extends GenericMessage<ModelSignal> {

    private ModelSignal payload;

    public ModelSignalMessage(ModelSignal payload) {
        super(payload);
    }

    public ModelSignalMessage(ModelSignal payload, MessageHeaders headers) {
        super(payload, headers);
    }
}
