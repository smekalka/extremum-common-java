package io.extremum.common.walk;

import io.extremum.common.attribute.Attribute;

/**
 * @author rpuch
 */
public interface AttributeVisitor {
    void visitAttribute(Attribute attribute);
}
