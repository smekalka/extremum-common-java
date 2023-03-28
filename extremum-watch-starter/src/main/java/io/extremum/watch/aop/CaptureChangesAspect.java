package io.extremum.watch.aop;

import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.watch.ModelSignal;
import io.extremum.sharedmodels.watch.ModelSignalMessage;
import io.extremum.watch.config.conditional.BlockingWatchConfiguration;
import io.extremum.watch.processor.CommonServiceWatchProcessor;
import io.extremum.watch.processor.MethodJoinPointInvocation;
import io.extremum.watch.processor.PatchFlowWatchProcessor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.Executor;

/**
 * Aspect to implement watch logic.
 * Have different pointcuts and different handlers to capture events.
 */
@Component
@Aspect
@Slf4j
@ConditionalOnBean(BlockingWatchConfiguration.class)
public class CaptureChangesAspect {
    private final PatchFlowWatchProcessor patchFlowProcessor;
    private final CommonServiceWatchProcessor commonServiceProcessor;
    private final Executor executor;
    private final SubscribableChannel modelSignalsMessageChannel;

    public CaptureChangesAspect(PatchFlowWatchProcessor patchFlowProcessor,
                                CommonServiceWatchProcessor commonServiceProcessor,
                                @Qualifier("watchEventsHandlingExecutor") Executor executor, SubscribableChannel modelSignalsMessageChannel) {
        this.patchFlowProcessor = patchFlowProcessor;
        this.commonServiceProcessor = commonServiceProcessor;
        this.executor = executor;
        this.modelSignalsMessageChannel = modelSignalsMessageChannel;
    }

    @Around("patchMethod()")
    public Object watchPatchChanges(ProceedingJoinPoint jp) throws Throwable {
        Model returnedModel = proceedWithPatchingFlagSet(jp);
        processAfterPatchEventSafely(jp, returnedModel);
        return returnedModel;
    }

    private Model proceedWithPatchingFlagSet(ProceedingJoinPoint jp) throws Throwable {
        WatchCaptureContext.enterPatching();
        Model returnedModel;
        try {
            returnedModel = (Model) jp.proceed();
        } finally {
            WatchCaptureContext.exitPatching();
        }
        return returnedModel;
    }

    private void processAfterPatchEventSafely(ProceedingJoinPoint jp, Model returnedModel) {
        try {
            executor.execute(() -> processPatchChanges(jp, returnedModel));
        } catch (Exception e) {
            log.error("Cannot execute advice logic", e);
        }
    }

    private void processPatchChanges(JoinPoint jp, Model returnedModel) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Watch PatchFlow method with name {} and args {}",
                        jp.getSignature().getName(), Arrays.toString(jp.getArgs()));
            }
            patchFlowProcessor.process(new MethodJoinPointInvocation(jp), returnedModel);
        } catch (Exception e) {
            log.error("Exception on watchPatchChanges() : ", e);
        }
    }

    @Around("commonServiceSaveMethods()")
    public Object watchCommonServiceSaves(ProceedingJoinPoint jp) throws Throwable {
        Boolean isNew = null;
        if (jp.getArgs().length > 0 && jp.getArgs()[0] instanceof BasicModel) {
            isNew = ((BasicModel<?>) jp.getArgs()[0]).getUuid() == null;
        }

        Model returnedModel = (Model) jp.proceed();

        if (isNew != null) {
            if (isNew) {
                modelSignalsMessageChannel.send(new ModelSignalMessage(new ModelSignal.Created(returnedModel)));
            } else {
                modelSignalsMessageChannel.send(new ModelSignalMessage(new ModelSignal.Updated(returnedModel)));
            }
        }

        if (!WatchCaptureContext.isPatching()) {
            executor.execute(() -> processCommonServiceInvocation(jp, returnedModel));
        }
        return returnedModel;
    }

    @AfterReturning(value = "commonServiceDeleteMethods()", returning = "returnedModel")
    public void watchCommonServiceDeletions(JoinPoint jp, Model returnedModel) {
        modelSignalsMessageChannel.send(new ModelSignalMessage(new ModelSignal.Deleted(returnedModel)));

        executor.execute(() -> processCommonServiceInvocation(jp, returnedModel));
    }

    private void processCommonServiceInvocation(JoinPoint jp, Model returnedModel) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Watch CommonService method with name {} and args {}",
                        jp.getSignature().getName(), Arrays.toString(jp.getArgs()));
            }
            commonServiceProcessor.process(new MethodJoinPointInvocation(jp), returnedModel);
        } catch (Exception e) {
            log.error("Exception on watchCommonServiceChanges() : ", e);
        }
    }

    @Pointcut("execution(* io.extremum.everything.services.management.PatchFlow+.patch(..))")
    private void patchMethod() {
    }

    @Pointcut("execution(* io.extremum.common.service.CommonService+.delete(..))")
    private void commonServiceDeleteMethods() {
    }

    @Pointcut("execution(* io.extremum.common.service.CommonService+.save(..))")
    private void commonServiceSaveMethods() {
    }

    // TODO: add ElasticsearchCommonService.patch(...) methods here?
}
