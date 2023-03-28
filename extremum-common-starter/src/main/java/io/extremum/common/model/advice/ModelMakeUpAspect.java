package io.extremum.common.model.advice;

import io.extremum.sharedmodels.basic.BasicModel;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Before;

public abstract class ModelMakeUpAspect<T extends BasicModel<?>> {
    protected abstract void applyToModel(T model);

    protected abstract void applyToModel(T nested, BasicModel<?> parent);

    @Before("execution(* io.extremum.common.service.ReactiveCommonService+.save(..))")
    @SuppressWarnings("unchecked")
    public void makeupModel(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length == 1) {
            T model = (T) joinPoint.getArgs()[0];
            applyToModel(model);

        }
        if (args.length == 2) {
            T nested = (T) joinPoint.getArgs()[0];
            applyToModel(nested, (BasicModel<?>) joinPoint.getArgs()[1]);
        }
    }
}
