package io.extremum.dynamic.everything.management;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.common.dto.converters.services.DynamicModelDtoConversionService;
import io.extremum.dynamic.ReactiveDescriptorDeterminator;
import io.extremum.dynamic.services.JsonBasedDynamicModelService;
import io.extremum.dynamic.watch.DynamicModelWatchService;
import io.extremum.everything.services.management.ReactiveEverythingManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReplaceReactiveEverythingManagementServiceBeanPostProcessor implements BeanPostProcessor {
    @Autowired
    ApplicationContext ctx;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (ReactiveEverythingManagementService.class.isAssignableFrom(bean.getClass())) {
            log.info("Bean of type {} found in spring context and will be replaced with bean of type {}",
                    ReactiveEverythingManagementService.class, HybridEverythingManagementService.class);

            return createHybridEverythingManagementServiceInstance((ReactiveEverythingManagementService) bean);
        } else {
            return bean;
        }
    }

    private HybridEverythingManagementService createHybridEverythingManagementServiceInstance(ReactiveEverythingManagementService defaultManagementService) {
        return new HybridEverythingManagementService(
                defaultManagementService,
                createDynamicModelEverythingServiceInstance(),
                ctx.getBean(ReactiveDescriptorDeterminator.class)
        );
    }

    private ReactiveDynamicModelEverythingManagementService createDynamicModelEverythingServiceInstance() {
        return new ReactiveDynamicModelEverythingManagementService(
                ctx.getBean(JsonBasedDynamicModelService.class),
                ctx.getBean(DynamicModelDtoConversionService.class),
                ctx.getBean(ObjectMapper.class),
                ctx.getBean(DynamicModelWatchService.class)
        );
    }
}
