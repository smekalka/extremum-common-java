package io.extremum.test.poll;

/**
 * @author rpuch
 */
public class PollTimedOutException extends RuntimeException {
    public PollTimedOutException(String message) {
        super(message);
    }
}
