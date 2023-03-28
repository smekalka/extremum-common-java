package io.extremum.watch.processor;

/**
 * @author rpuch
 */
class TestInvocation implements Invocation {
    private final String name;
    private final Object[] arguments;

    TestInvocation(String name, Object[] arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    @Override
    public String methodName() {
        return name;
    }

    @Override
    public Object[] args() {
        return arguments;
    }
}
