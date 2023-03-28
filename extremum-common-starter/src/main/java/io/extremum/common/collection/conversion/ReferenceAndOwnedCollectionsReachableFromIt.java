package io.extremum.common.collection.conversion;

import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import io.extremum.sharedmodels.fundamental.CollectionReference;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ReferenceAndOwnedCollectionsReachableFromIt implements ReferenceCollector {
    private final CollectionReference<?> reference;
    private final CollectionDescriptor collectionDescriptor;

    @Override
    public List<ReferenceContext> collectReferences(Object object) {
        List<ReferenceContext> collectedRefs = new ArrayList<>();

        MakeupBrush brush = new PreExistingCollectionReferenceBrush(collectionDescriptor);
        collectedRefs.add(new ReferenceContext(reference, brush));

        if (reference.getTop() != null) {
            collectReferencesFromTopTo(collectedRefs);
        }

        return collectedRefs;
    }

    private void collectReferencesFromTopTo(List<ReferenceContext> collectedRefs) {
        for (Object topElement : reference.getTop()) {
            if (topElement instanceof ResponseDto) {
                ResponseDto responseDto = (ResponseDto) topElement;
                collectedRefs.addAll(new OwnedCollectionReferenceCollector().collectReferences(responseDto));
            }
        }
    }
}
