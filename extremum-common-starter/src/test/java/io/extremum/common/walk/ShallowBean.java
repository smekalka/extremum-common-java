package io.extremum.common.walk;

/**
 * @author rpuch
 */
class ShallowBean {
    private final String str = "abc";
    private final long primitiveLong = 10;
    private final Integer integer = 20;
    private final Object obj = null;

    private static final String SHOULD_NOT_BE_VISITED = "I should not be visited";
}
