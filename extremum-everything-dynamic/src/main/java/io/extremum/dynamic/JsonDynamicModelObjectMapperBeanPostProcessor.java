package io.extremum.dynamic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class JsonDynamicModelObjectMapperBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ObjectMapper) {
            ObjectMapper mapper = (ObjectMapper) bean;
            SimpleFilterProvider filterProvider = new SimpleFilterProvider();
            filterProvider.addFilter("dynamicModelFilter",
                    SimpleBeanPropertyFilter.serializeAllExcept("model", "created", "modified", "version"));
            mapper.setFilterProvider(filterProvider);

        }

        return bean;
    }
}
