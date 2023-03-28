package io.extremum.sku.aop;

import io.extremum.sku.aop.annotation.SkuMetric;
import io.extremum.sku.model.SkuID;
import io.extremum.sku.model.SkuMetricMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.MessageChannel;

@Aspect
@Slf4j
@AllArgsConstructor
public class SkuMetricAspect {

    private final ApplicationContext applicationContext;

    @Before(value = "@within( io.extremum.sku.aop.annotation.SkuMetric) || @annotation(io.extremum.sku.aop.annotation.SkuMetric)")
    public void before(JoinPoint joinPoint) {

        SkuMetric annotation;
        annotation = (SkuMetric) joinPoint.getSignature().getDeclaringType().getAnnotation(SkuMetric.class);
        if (joinPoint.getSignature() instanceof MethodSignature) {
            SkuMetric methodAnnotation = ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(SkuMetric.class);
            if (methodAnnotation != null) {
                annotation = methodAnnotation;
            }
        }

        if (annotation.returnValue()) {
            return;
        }

        long amount = annotation.amount();
        SkuID sku = annotation.sku();
        long skuValue = annotation.sku().getValue();
        String channel = annotation.channel();
        if (sku == SkuID.UNSPECIFIED) {
            skuValue = annotation.custom();
        }
        log.debug("Collect sku {} metrics for class: {}, method: {} amount: {}", sku, joinPoint.getSignature().getDeclaringType().getSimpleName(), joinPoint.getSignature().getName(), amount);
        MessageChannel messageChannel = applicationContext.getBean(channel, MessageChannel.class);
        messageChannel.send(new SkuMetricMessage(new io.extremum.sku.model.SkuMetric(skuValue, amount, applicationContext.getId())));
    }


    @AfterReturning(pointcut = "@annotation(io.extremum.sku.aop.annotation.SkuMetric)", returning = "result")
    public void collectMetricAfter(JoinPoint joinPoint, Object result) {
        SkuMetric methodAnnotation = ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(SkuMetric.class);
        if (!methodAnnotation.returnValue()) {
            return;
        }
        if (!(result instanceof Number)) {
            throw new IllegalArgumentException("Returning value of @SkuMetric must be instance of " + Number.class.getName());
        }

        SkuID sku = methodAnnotation.sku();
        long skuValue = methodAnnotation.sku().getValue();
        if (sku == SkuID.UNSPECIFIED) {
            skuValue = methodAnnotation.custom();
        }
        log.debug(
                "Collect sku {} metrics for class: {}, method: {} amount: {}", sku,
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(), ((Number) result).longValue()
        );
        MessageChannel messageChannel = applicationContext.getBean(methodAnnotation.channel(), MessageChannel.class);
        messageChannel.send(new SkuMetricMessage(new io.extremum.sku.model.SkuMetric(skuValue, ((Number) result).longValue(), applicationContext.getId())));
    }
}