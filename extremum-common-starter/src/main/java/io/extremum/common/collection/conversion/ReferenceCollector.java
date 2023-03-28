package io.extremum.common.collection.conversion;

import java.util.List;

interface ReferenceCollector {
    List<ReferenceContext> collectReferences(Object object);
}
