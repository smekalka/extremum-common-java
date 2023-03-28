package io.extremum.common.walk;

import io.extremum.common.attribute.FieldAttribute;
import io.extremum.common.utils.InstanceFields;
import io.extremum.common.utils.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * @author rpuch
 */
public final class ShallowFieldGraphWalker implements AttributeGraphWalker {
    @Override
    public void walk(Object root, AttributeVisitor visitor) {
        Objects.requireNonNull(root, "Root cannot be null");
        Objects.requireNonNull(visitor, "Visitor cannot be null");

        new InstanceFields(root.getClass()).stream()
                .forEach(field -> visitField(field, root, visitor));
    }

    private void visitField(Field field, Object root, AttributeVisitor visitor) {
        Object value = ReflectionUtils.getFieldValue(field, root);
        visitor.visitAttribute(new FieldAttribute(field, value));
    }
}
