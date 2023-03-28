package io.extremum.everything.aop;

import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.common.exceptions.ModelNotFoundException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Controller;

/**
 * This aspect checks whether &#064;{@link Controller}s annotated with
 * &#064;ConvertNullDescriptorToModelNotFound have any {@link Descriptor}
 * parameter to which null is passed. If this is true, {@link ModelNotFoundException}
 * is thrown.
 * The intention is to have possibility to return 404 code when a non-existing
 * descriptor ID is passed to our APIs.
 *
 * @author rpuch
 */
@Aspect
public class ConvertNullDescriptorToModelNotFoundAspect {
    /**
     * This means 'method calls on instances of classes annotated with @Controller
     * directly or via a meta-annotated annotation (with one level of indirection at max).
     */
    @Pointcut("" +
            "within(@org.springframework.stereotype.Controller *) || " +
            "within(@(@org.springframework.stereotype.Controller *) *)")
    private void isController() {}

    /**
     * This means 'method calls on instances of classes annotated with @ConvertNullDescriptorToModelNotFound
     * directly or via a meta-annotated annotation (with one level of indirection at max).
     */
    @Pointcut("" +
            "within(@io.extremum.everything.aop.ConvertNullDescriptorToModelNotFound *) || " +
            "within(@(@io.extremum.everything.aop.ConvertNullDescriptorToModelNotFound *) *)")
    private void annotatedToConvert() {}

    @Around("isController() && annotatedToConvert()")
    public Object executeAroundController(ProceedingJoinPoint point) throws Throwable {
        throwIfNullDescriptorIsPassed(point);
        return point.proceed();
    }

    private void throwIfNullDescriptorIsPassed(ProceedingJoinPoint point) {
        if (!(point.getSignature() instanceof MethodSignature)) {
            return;
        }

        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        Class[] parameterTypes = methodSignature.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class parameterType = parameterTypes[i];
            if (parameterType == Descriptor.class && point.getArgs()[i] == null) {
                throw new ModelNotFoundException("No descriptor was found");
            }
        }
    }
}
