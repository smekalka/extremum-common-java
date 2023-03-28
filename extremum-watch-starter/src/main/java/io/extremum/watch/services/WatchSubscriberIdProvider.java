package io.extremum.watch.services;

import java.util.Optional;

public interface WatchSubscriberIdProvider {
    Optional<String> getSubscriberId();
}
