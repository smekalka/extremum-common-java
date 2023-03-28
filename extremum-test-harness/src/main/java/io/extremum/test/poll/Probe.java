package io.extremum.test.poll;

/**
 * @author rpuch
 */
public interface Probe<T> {
    T sample();

    boolean isFinished(T value);

    default void doOnFailure() {
        // doing nothing by default
    }
}
