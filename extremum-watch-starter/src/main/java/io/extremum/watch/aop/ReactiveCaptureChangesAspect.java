package io.extremum.watch.aop;

import io.extremum.everything.services.management.ReactivePatchFlow;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.watch.config.conditional.ReactiveWatchConfiguration;
import io.extremum.watch.processor.*;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Arrays;

/**
 * Aspect to implement watch logic.
 * Have different pointcuts and different handlers to capture events.
 */
@Component
@Aspect
@Slf4j
@ConditionalOnBean(ReactiveWatchConfiguration.class)
public class ReactiveCaptureChangesAspect {
    private final ReactivePatchFlowWatchProcessor patchFlowProcessor;
    private final ReactiveCommonServiceWatchProcessor commonServiceProcessor;

    public ReactiveCaptureChangesAspect(ReactivePatchFlowWatchProcessor patchFlowProcessor,
                                        ReactiveCommonServiceWatchProcessor commonServiceProcessor) {
        this.patchFlowProcessor = patchFlowProcessor;
        this.commonServiceProcessor = commonServiceProcessor;
    }

    @Around("patchMethod()")
    public Object watchPatchChanges(ProceedingJoinPoint jp) throws Throwable {
        @SuppressWarnings("unchecked")
        Mono<? extends Model> returnedModelMono = (Mono<? extends Model>) jp.proceed();
        return processAfterPatchEventSafely(jp, returnedModelMono);
    }

    private Mono<? extends Model> processAfterPatchEventSafely(ProceedingJoinPoint jp, Mono<? extends Model> returnedModelMono) {
        return withContext(returnedModelMono).map(result -> {
            ReactivePatchFlow.isPatching(result.getT1(), result.getT2()).flatMap(isPatching -> {
                if (!isPatching) {
                    return processPatchChanges(jp, result.getT1());
                } else {
                    return Mono.empty();
                }
            }).subscribe();
            return result.getT1();
        });
   }

    private Mono<Void> processPatchChanges(JoinPoint jp, Model returnedModel) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Watch PatchFlow method with name {} and args {}",
                        jp.getSignature().getName(), Arrays.toString(jp.getArgs()));
            }
            return patchFlowProcessor.process(new MethodJoinPointInvocation(jp), returnedModel);
        } catch (Exception e) {
            log.error("Exception on watchPatchChanges() : ", e);
            return Mono.empty();
        }
    }

    @Around("commonServiceSaveMethods()")
    public Object watchCommonServiceSaves(ProceedingJoinPoint jp) throws Throwable {
        @SuppressWarnings("unchecked")
        Mono<? extends Model> returnedModelMono = (Mono<? extends Model>) jp.proceed();
        return withContext(returnedModelMono).map(result -> {
            ReactivePatchFlow.isPatching(result.getT1(), result.getT2()).flatMap(isPatching -> {
                if (!isPatching) {
                    return processCommonServiceInvocation(jp, result.getT1());
                } else {
                    return Mono.empty();
                }
            }).subscribe();
            return result.getT1();
        });
    }

    @Around("commonServiceDeleteMethods()")
    public Object watchCommonServiceDeletions(ProceedingJoinPoint jp) throws Throwable {
        @SuppressWarnings("unchecked")
        Mono<? extends Model> returnedModelMono = (Mono<? extends Model>) jp.proceed();
        return returnedModelMono
                .doOnSuccess(returnedModel -> processCommonServiceInvocation(jp, returnedModel).subscribe());
    }

    private Mono<Void> processCommonServiceInvocation(JoinPoint jp, Model returnedModel) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Watch CommonService method with name {} and args {}",
                        jp.getSignature().getName(), Arrays.toString(jp.getArgs()));
            }
            return commonServiceProcessor.process(new MethodJoinPointInvocation(jp), returnedModel);
        } catch (Exception e) {
            log.error("Exception on watchCommonServiceChanges() : ", e);
            return Mono.empty();
        }
    }

    private static <T> Mono<Tuple2<T, Context>> withContext(Mono<T> mono) {
        return mono.flatMap(result -> Mono.subscriberContext().map(ctx -> Tuples.of(result, ctx)));
    }

    @Pointcut("execution(* io.extremum.everything.services.management.ReactivePatchFlow+.patch(..))")
    private void patchMethod() {
    }

    @Pointcut("execution(* io.extremum.common.service.ReactiveCommonService+.delete(..))")
    private void commonServiceDeleteMethods() {
    }

    @Pointcut("execution(* io.extremum.common.service.ReactiveCommonService+.save(..))")
    private void commonServiceSaveMethods() {
    }

    // TODO: add ElasticsearchCommonService.patch(...) methods here?
}

