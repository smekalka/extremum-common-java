package io.extremum.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class FindUtils {
    @SuppressWarnings("unchecked")
    private static <M extends Class> Collection<M> findCandidates(ClassPathScanningCandidateComponentProvider provider, String scanPackage, M resultClass) {
        List<M> resultList = new ArrayList<>();
        for (BeanDefinition definition : provider.findCandidateComponents(scanPackage)) {
            try {
                resultList.add((M) Class.forName(definition.getBeanClassName()));
                log.info("Class found : {}", definition.getBeanClassName());
            } catch (ClassNotFoundException e) {
                log.error("Class not found for name: {}", definition.getBeanClassName());
            }
        }
        return resultList;
    }

    public static <M extends Class> Collection<M> findClassesByInterface(M interfaceClass, String scanPackage) {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(interfaceClass));
        return findCandidates(provider, scanPackage, interfaceClass);
    }

    public static <M extends Class> Collection<M> findClassesByAnnotation(M resultSuperClass, Class<? extends Annotation> annotationClass, String scanPackage) {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(annotationClass));
        return findCandidates(provider, scanPackage, resultSuperClass);
    }
}
