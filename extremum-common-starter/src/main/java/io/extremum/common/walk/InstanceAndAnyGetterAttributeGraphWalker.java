package io.extremum.common.walk;

import io.extremum.common.attribute.AnyGetterAttributes;
import io.extremum.common.attribute.Attribute;
import io.extremum.common.attribute.InstanceAttributes;

import java.util.Objects;
import java.util.stream.Stream;

public class InstanceAndAnyGetterAttributeGraphWalker implements AttributeGraphWalker {

    @Override
    public void walk(Object root, AttributeVisitor visitor) {
        Objects.requireNonNull(root, "Root cannot be null");
        Objects.requireNonNull(visitor, "Visitor cannot be null");

        Stream.concat(
                        new InstanceAttributes(root).stream(),
                        new AnyGetterAttributes(root).stream()
                )
                .forEach(attribute -> visitAttribute(attribute, visitor));
    }

    private void visitAttribute(Attribute attribute, AttributeVisitor visitor) {
        visitor.visitAttribute(attribute);
    }
}
