package io.extremum.common.walk;

/**
 * @author rpuch
 */
public interface ObjectContentsGraphWalker {
    void walk(Object root, ObjectVisitor visitor);
}
