package io.extremum.common.model.advice;

import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.sharedmodels.basic.Model;
import lombok.SneakyThrows;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Before;
import reactor.core.publisher.Mono;

public abstract class ReactiveModelLifecycleAspect<M extends BasicModel<?>> {

    @Before("execution(* io.extremum.common.service.ReactiveCommonService+.save(..))")
    @SuppressWarnings("unchecked")
    public void onBeforeModelSaved(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length == 1) {
            M model = (M) joinPoint.getArgs()[0];
            onBeforeModelSaved(model);

        }
        if (args.length == 2) {
            M nested = (M) joinPoint.getArgs()[0];
            onBeforeModelSaved(nested, (BasicModel<?>) joinPoint.getArgs()[1]);
        }
    }

    @Around("execution(* io.extremum.common.service.ReactiveCommonService+.save(..))")
    @SuppressWarnings("unchecked")
    @SneakyThrows
    public Mono<? extends Model> onAfterModelSaved(ProceedingJoinPoint joinPoint) {
        @SuppressWarnings("unchecked")
        Mono<? extends Model> returnedModelMono = (Mono<? extends Model>) joinPoint.proceed();
        return returnedModelMono.doOnNext(model -> {
            Object[] args = joinPoint.getArgs();
            if (args.length == 1) {
                M model2 = (M) joinPoint.getArgs()[0];
                onAfterModelSaved(model2).subscribe();

            }
            if (args.length == 2) {
                M nested = (M) joinPoint.getArgs()[0];
                onAfterModelSaved(nested, (BasicModel<?>) joinPoint.getArgs()[1]).subscribe();
            }
        });
    }

    protected abstract Mono<Void> onAfterModelSaved(M model);

    protected abstract Mono<Void> onAfterModelSaved(M nested, BasicModel<?> arg);

    protected abstract void onBeforeModelSaved(M nested, BasicModel<?> arg);

    protected abstract void onBeforeModelSaved(M model);

}
