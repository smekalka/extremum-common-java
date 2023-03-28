package io.extremum.common.collection.visit;

import io.extremum.common.attribute.Attribute;
import io.extremum.sharedmodels.dto.ResponseDto;
import io.extremum.sharedmodels.fundamental.CollectionReference;

public interface CollectionVisitor {
    void visit(CollectionReference<?> reference, Attribute attribute, ResponseDto responseDto);
}
