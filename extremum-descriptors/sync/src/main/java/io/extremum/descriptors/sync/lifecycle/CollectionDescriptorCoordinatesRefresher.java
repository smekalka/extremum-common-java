package io.extremum.descriptors.sync.lifecycle;

import io.extremum.sharedmodels.descriptor.Descriptor;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;

/**
 * @author rpuch
 */
public class CollectionDescriptorCoordinatesRefresher implements BeforeConvertCallback<Descriptor> {
    @Override
    public Descriptor onBeforeConvert(Descriptor descriptor, String collection) {
        if (descriptor.isCollection() && descriptor.getCollection() != null) {
            descriptor.getCollection().refreshCoordinatesString();
        }
        return descriptor;
    }
}
