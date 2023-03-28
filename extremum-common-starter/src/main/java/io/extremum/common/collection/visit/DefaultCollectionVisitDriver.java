package io.extremum.common.collection.visit;

import io.extremum.common.attribute.Attribute;
import io.extremum.common.walk.AttributeGraphWalker;
import io.extremum.common.walk.AttributeVisitor;
import io.extremum.common.walk.DeepContentsGraphWalker;
import io.extremum.common.walk.ObjectContentsGraphWalker;
import io.extremum.common.walk.ObjectVisitor;
import io.extremum.common.walk.VisitDirection;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import io.extremum.sharedmodels.fundamental.CollectionReference;

public class DefaultCollectionVisitDriver implements CollectionVisitDriver {

    private final CollectionVisitor collectionVisitor;

    private final ObjectContentsGraphWalker deepWalker;
    private final AttributeGraphWalker attributeGraphWalker;

    public DefaultCollectionVisitDriver(VisitDirection visitDirection, CollectionVisitor collectionVisitor, AttributeGraphWalker attributeGraphWalker) {
        deepWalker = new DeepContentsGraphWalker(visitDirection, 10, DefaultCollectionVisitDriver::shouldGoDeeper);
        this.collectionVisitor = collectionVisitor;
        this.attributeGraphWalker = attributeGraphWalker;
    }

    private static boolean shouldGoDeeper(Object object) {
        return object != null && (!(object instanceof Descriptor));
    }

    @Override
    public void visitCollectionsInResponseDto(ResponseDto dto) {
        walkResponseDtoWithoutRecursion(dto);

        ObjectVisitor dtoVisitor = this::walkResponseDtoInAttribute;
        deepWalker.walk(dto, new IsResponseDto(dtoVisitor));
    }

    private void walkResponseDtoInAttribute(Object object) {
        ResponseDto dto = (ResponseDto) object;
        if (dto == null) {
            return;
        }

        walkResponseDtoWithoutRecursion(dto);
    }

    private void walkResponseDtoWithoutRecursion(ResponseDto dto) {
        AttributeVisitor visitor = attribute -> visitCollection(attribute, dto);
        attributeGraphWalker.walk(dto, new IsCollectionReference(visitor));
    }

    private void visitCollection(Attribute attribute, ResponseDto dto) {
        if (attribute.value() == null) {
            return;
        }

        CollectionReference<?> reference = (CollectionReference<?>) attribute.value();

        collectionVisitor.visit(reference, attribute, dto);
    }

    private static class IsResponseDto implements ObjectVisitor {
        private final ObjectVisitor visitor;

        private IsResponseDto(ObjectVisitor visitor) {
            this.visitor = visitor;
        }

        @Override
        public void visit(Object object) {
            if (ResponseDto.class.isAssignableFrom(object.getClass())) {
                visitor.visit(object);
            }
        }
    }

    private static class IsCollectionReference implements AttributeVisitor {
        private final AttributeVisitor visitor;

        private IsCollectionReference(AttributeVisitor visitor) {
            this.visitor = visitor;
        }

        @Override
        public void visitAttribute(Attribute attribute) {
            if (isOfTypeCollectionReference(attribute)) {
                visitor.visitAttribute(attribute);
            }
        }

        private boolean isOfTypeCollectionReference(Attribute attribute) {
            return CollectionReference.class.isAssignableFrom(attribute.type());
        }
    }
}
