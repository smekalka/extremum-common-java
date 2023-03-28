package io.extremum.common.logging;

import io.extremum.sharedmodels.dto.Response;
import io.extremum.sharedmodels.logging.LoggingConstants;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import reactor.core.publisher.Mono;

@Aspect
public class ReactiveFillRequestIdOnResponseAspect {
    @Around("(isController() || isControllerAdvice()) && returnsMono()")
    public Object executeAroundController(ProceedingJoinPoint point) throws Throwable {
        Object invocationResult = point.proceed();

        if (invocationResult instanceof Mono) {
            return monoWithRequestId((Mono<?>) invocationResult);
        }

        return invocationResult;
    }

    private Object monoWithRequestId(Mono<?> mono) {
        return mono.flatMap(this::applyRequestIdOnObjectIfItIsResponse);
    }

    private Mono<?> applyRequestIdOnObjectIfItIsResponse(Object object) {
        if (object instanceof Response) {
            Response response = (Response) object;
            return Mono.subscriberContext()
                    .map(context -> context.getOrDefault(LoggingConstants.REQUEST_ID_ATTRIBUTE_NAME, "<none>"))
                    .map(response::withRequestId);
        }
        return Mono.just(object);
    }

    @Pointcut("" +
            "within(@org.springframework.stereotype.Controller *) || " +
            "within(@(@org.springframework.stereotype.Controller *) *)")
    private void isController() {
    }

    @Pointcut("" +
            "within(@org.springframework.web.bind.annotation.ControllerAdvice *) || " +
            "within(@(@org.springframework.web.bind.annotation.ControllerAdvice *) *)")
    private void isControllerAdvice() {
    }

    @Pointcut("execution(reactor.core.publisher.Mono *..*.*(..))")
    private void returnsMono() {
    }
}
