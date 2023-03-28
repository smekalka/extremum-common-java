package io.extremum.common.support;

import io.extremum.common.service.ReactiveCommonService;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.common.service.CommonService;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.ResolvableType;

/**
 * @author rpuch
 */
class CommonServiceUtils {

    static Class<Model> findCommonServiceModelClass(CommonService<?> service) {
        ResolvableType commonServiceInterface = findCommonServiceInterface(service, CommonService.class);
        return findModelGeneric(commonServiceInterface, service);
    }

    static Class<Model> findReactiveCommonServiceModelClass(ReactiveCommonService<?> service) {
        ResolvableType reactiveCommonServiceInterface = findCommonServiceInterface(service, ReactiveCommonService.class);
        return findModelGeneric(reactiveCommonServiceInterface, service);
    }

    private static ResolvableType findCommonServiceInterface(Object service, Class<?> baseInterfaceClass) {
        ResolvableType currentType = ResolvableType.forClass(AopUtils.getTargetClass(service));
        ResolvableType commonServiceInterface = ResolvableType.NONE;

        do {
            for (ResolvableType iface : currentType.getInterfaces()) {
                if (iface.getRawClass() == baseInterfaceClass) {
                    commonServiceInterface = iface;
                    break;
                }
            }
            currentType = currentType.getSuperType();
        } while (commonServiceInterface == ResolvableType.NONE && currentType != ResolvableType.NONE);

        return commonServiceInterface;
    }

    private static Class<Model> findModelGeneric(ResolvableType commonServiceInterface, Object service) {
        for (ResolvableType generic : commonServiceInterface.getGenerics()) {
            Class<?> resolvedGeneric = generic.resolve();
            if (resolvedGeneric != null && Model.class.isAssignableFrom(resolvedGeneric)) {
                @SuppressWarnings("unchecked")
                Class<Model> castGeneric = (Class<Model>) resolvedGeneric;
                return castGeneric;
            }
        }

        throw new IllegalStateException("For class " + service.getClass() + " did not find model generic");
    }
}
