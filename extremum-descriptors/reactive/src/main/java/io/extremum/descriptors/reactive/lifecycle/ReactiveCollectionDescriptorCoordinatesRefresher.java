package io.extremum.descriptors.reactive.lifecycle;

import io.extremum.sharedmodels.descriptor.Descriptor;
import org.reactivestreams.Publisher;
import org.springframework.data.mongodb.core.mapping.event.ReactiveBeforeConvertCallback;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public class ReactiveCollectionDescriptorCoordinatesRefresher
        implements ReactiveBeforeConvertCallback<Descriptor> {
    @Override
    public Publisher<Descriptor> onBeforeConvert(Descriptor descriptor, String collection) {
        return Mono.fromSupplier(() -> {
            if (descriptor.isCollection() && descriptor.getCollection() != null) {
                descriptor.getCollection().refreshCoordinatesString();
            }
            return descriptor;
        });
    }
}
