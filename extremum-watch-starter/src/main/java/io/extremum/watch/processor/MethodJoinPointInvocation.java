package io.extremum.watch.processor;

import org.aspectj.lang.JoinPoint;

/**
 * @author rpuch
 */
public class MethodJoinPointInvocation implements Invocation {
    private final JoinPoint joinPoint;

    public MethodJoinPointInvocation(JoinPoint joinPoint) {
        this.joinPoint = joinPoint;
    }

    @Override
    public String methodName() {
        return joinPoint.getSignature().getName();
    }

    @Override
    public Object[] args() {
        return joinPoint.getArgs();
    }
}
