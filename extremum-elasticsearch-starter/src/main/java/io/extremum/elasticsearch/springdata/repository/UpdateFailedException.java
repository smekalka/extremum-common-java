package io.extremum.elasticsearch.springdata.repository;

/**
 * @author rpuch
 */
public class UpdateFailedException extends RuntimeException {
    public UpdateFailedException(String message) {
        super(message);
    }
}
