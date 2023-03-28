package io.extremum.mongo.facilities;

import io.extremum.sharedmodels.descriptor.Descriptor;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

/**
 * @author rpuch
 */
public interface MongoDescriptorFacilities {
    Descriptor create(ObjectId id, String modelType, String iri, Map<String, Object> preview);

    Descriptor fromInternalId(ObjectId internalId);

    Descriptor fromInternalId(String internalId);

    List<String> getInternalIdList(List<Descriptor> descriptors);

    List<Descriptor> fromInternalIdListOrNull(List<String> internalIdList);

    ObjectId resolve(Descriptor descriptor);
}
