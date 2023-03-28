package io.extremum.common.secondaryds;

import io.extremum.sharedmodels.basic.Model;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.data.repository.Repository;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class ModelAnnotatedFilter implements TypeFilter {
    private final Class<? extends Annotation> modelAnnotationClass;

    @Override
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)
            throws IOException {
        if (!hasTypeInHierarchy(Repository.class.getName(), metadataReader, metadataReaderFactory)) {
            // an optimization: if there is no Repository interface among super-interfaces, we don't
            // bother loading the class for inspection
            return false;
        }

        return hasModelClassInRepositoryClassGenerics(metadataReader);
    }

    private boolean hasTypeInHierarchy(String targetClassName, MetadataReader metadataReader,
                                       MetadataReaderFactory metadataReaderFactory) throws IOException {
        ClassMetadata classMetadata = metadataReader.getClassMetadata();
        if (targetClassName.equals(classMetadata.getClassName())) {
            return true;
        }

        if (classMetadata.getSuperClassName() != null) {
            MetadataReader superclassReader = metadataReaderFactory.getMetadataReader(
                    classMetadata.getSuperClassName());
            if (hasTypeInHierarchy(targetClassName, superclassReader, metadataReaderFactory)) {
                return true;
            }
        }

        for (String superInterfaceName : classMetadata.getInterfaceNames()) {
            MetadataReader superInterfaceReader = metadataReaderFactory.getMetadataReader(superInterfaceName);
            if (hasTypeInHierarchy(targetClassName, superInterfaceReader, metadataReaderFactory)) {
                return true;
            }
        }

        return false;
    }

    private boolean hasModelClassInRepositoryClassGenerics(MetadataReader metadataReader) {
        Class<?> repositoryClass = loadInitialClass(metadataReader);
        if (repositoryClass == null) {
            return false;
        }

        Class<?> modelType = findModelType(repositoryClass);
        if (modelType == null) {
            return false;
        }

        return AnnotationUtils.findAnnotation(modelType, modelAnnotationClass) != null;
    }

    @Nullable
    private Class<?> loadInitialClass(MetadataReader metadataReader) {
        try {
            return Class.forName(metadataReader.getClassMetadata().getClassName(), false, getClassLoader());
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private ClassLoader getClassLoader() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = getClass().getClassLoader();
        }
        return loader;
    }

    private Class<?> findModelType(Class<?> repositoryClass) {
        List<Type> genericTypertypes = new ArrayList<>();
        if (repositoryClass.getGenericSuperclass() != null) {
            genericTypertypes.add(repositoryClass.getGenericSuperclass());
        }
        genericTypertypes.addAll(Arrays.asList(repositoryClass.getGenericInterfaces()));
        return findModelType(genericTypertypes);
    }

    private Class<?> findModelType(List<Type> genericTypertypes) {
        for (Type genericTypertype : genericTypertypes) {
            Class<?> modelClass = findModelType(genericTypertype);
            if (modelClass != null) {
                return modelClass;
            }
        }
        return null;
    }

    private Class<?> findModelType(Type genericTypertype) {
        Class<?> modelType = null;
        if (genericTypertype instanceof ParameterizedType) {
            for (Type typeArg : ((ParameterizedType) genericTypertype).getActualTypeArguments()) {
                Class<?> typeArgClass = (Class<?>) typeArg;
                if (Model.class.isAssignableFrom(typeArgClass)) {
                    modelType = typeArgClass;
                    break;
                }
            }
        }
        return modelType;
    }
}
