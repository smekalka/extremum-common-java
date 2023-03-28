package io.extremum.watch.processor;

import io.extremum.watch.models.TextWatchEvent;

/**
 * @author rpuch
 */
public interface WatchEventConsumer {
    void consume(TextWatchEvent event);
}
