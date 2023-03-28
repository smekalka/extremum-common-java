package io.extremum.common.collection.service;

import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

/**
 * An override that can be used to make a model instance to be viewed as a collection
 * in a context that expects a collection. For example, there could exist a model called
 * Set that, by its design, is thought of as a collection of some Elements.
 * So, when streaming the contents of the Set, it may be desirable to use the ID of the Set
 * itself, instead of creating a separate collection of its Elements with a distinct ID.
 *
 * @author rpuch
 */
public interface ReactiveCollectionOverride {
    /**
     * Returns true whether this override supports the given descriptor (that is, whether
     * it knows how to turn this descriptor to a collection).
     *
     * @param descriptor descriptor to check
     * @return true if the descriptor is supported
     */
    boolean supports(Descriptor descriptor);

    /**
     * Returns the descriptor type in the GET context. The descriptor passed here must be supported
     * by this override (that is, this override has already returned true when {@link #supports(Descriptor)}
     * was called with the given Descriptor).
     *
     * @param descriptor descriptor to work with
     * @return the type
     */
    Mono<Descriptor.Type> typeForGetOperation(Descriptor descriptor);

    /**
     * Returns a collection that this Descriptor's model represents (from the point of view of this
     * override). To continue the example outlined in the class-level javadoc, this method would
     * return an owned collection where host ID would match the descriptor passed in (that is,
     * the Set id), and the attribute name would depend on the Set structure.
     *
     * @param descriptor descriptor of the model for which the override is made
     * @return collection descriptor
     */
    Mono<CollectionDescriptor> extractCollectionFromDescriptor(Descriptor descriptor);
}
