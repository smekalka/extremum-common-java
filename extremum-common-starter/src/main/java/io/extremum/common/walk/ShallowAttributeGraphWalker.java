package io.extremum.common.walk;

import io.extremum.common.attribute.Attribute;
import io.extremum.common.attribute.InstanceAttributes;

import java.util.Objects;

/**
 * @author rpuch
 */
public final class ShallowAttributeGraphWalker implements AttributeGraphWalker {
    @Override
    public void walk(Object root, AttributeVisitor visitor) {
        Objects.requireNonNull(root, "Root cannot be null");
        Objects.requireNonNull(visitor, "Visitor cannot be null");

        new InstanceAttributes(root).stream()
                .forEach(attribute -> visitAttribute(attribute, visitor));
    }

    private void visitAttribute(Attribute attribute, AttributeVisitor visitor) {
        visitor.visitAttribute(attribute);
    }
}
