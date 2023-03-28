package io.extremum.common.descriptor.resolve;

import io.extremum.common.walk.DeepContentsGraphWalker;
import io.extremum.common.walk.ObjectContentsGraphWalker;
import io.extremum.common.walk.ObjectVisitor;
import io.extremum.common.walk.VisitDirection;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class IntrospectingResponseDtoDescriptorResolver implements ResponseDtoDescriptorResolver {
    private final ReactiveDescriptorResolver descriptorResolver;

    private final ObjectContentsGraphWalker walker = new DeepContentsGraphWalker(VisitDirection.ROOT_TO_LEAVES, 10,
            this::shouldGoDeeper);

    private boolean shouldGoDeeper(Object o) {
        return o != null && !(o instanceof Descriptor);
    }

    @Override
    public Mono<Void> resolveExternalIdsIn(ResponseDto responseDto) {
        List<Descriptor> descriptors = new ArrayList<>();

        walker.walk(responseDto, new IsDescriptor(object -> descriptors.add((Descriptor) object)));

        return descriptorResolver.resolveExternalIds(descriptors);
    }

    private static class IsDescriptor implements ObjectVisitor {
        private final ObjectVisitor visitor;

        private IsDescriptor(ObjectVisitor visitor) {
            this.visitor = visitor;
        }

        @Override
        public void visit(Object object) {
            if (Descriptor.class.isAssignableFrom(object.getClass())) {
                visitor.visit(object);
            }
        }
    }
}
