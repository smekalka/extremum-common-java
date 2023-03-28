package io.extremum.common.walk;

/**
 * @author rpuch
 */
public interface AttributeGraphWalker {
    void walk(Object root, AttributeVisitor visitor);
}
