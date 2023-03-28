package io.extremum.descriptors.common;

import io.extremum.sharedmodels.content.Display;
import io.extremum.sharedmodels.descriptor.CollectionCoordinates;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.OwnedModelDescriptor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
import org.springframework.data.mapping.model.FieldNamingStrategy;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.BasicMongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.CachingMongoPersistentProperty;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.data.util.TypeInformation;
import reactor.core.publisher.Mono;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

class CustomDescriptorCollectionMappingContext extends MongoMappingContext {
    private FieldNamingStrategy fieldNamingStrategy;

    @Override
    public void setFieldNamingStrategy(FieldNamingStrategy fieldNamingStrategy) {
        super.setFieldNamingStrategy(fieldNamingStrategy);
        this.fieldNamingStrategy = fieldNamingStrategy;
    }

    @Override
    protected <T> BasicMongoPersistentEntity<T> createPersistentEntity(TypeInformation<T> typeInformation) {
        if (typeInformation.getType() == Descriptor.class || typeInformation.getType() == CollectionDescriptor.class) {
            return new CustomCollectionMappingMongoPersistentEntity<>(typeInformation);
        }

        return super.createPersistentEntity(typeInformation);
    }

    @Override
    public MongoPersistentProperty createPersistentProperty(Property property, MongoPersistentEntity<?> owner, SimpleTypeHolder simpleTypeHolder) {
        if (owner.getType() == Descriptor.class) {
            return new MirroringCachingMongoPersistentProperty(property, owner, simpleTypeHolder) {
                @Override
                Property property() {
                    return property;
                }

                @Override
                Class<?> entityMirrorClass() {
                    return DescriptorMirror.class;
                }
            };
        }
        if (owner.getType() == CollectionDescriptor.class) {
            return new MirroringCachingMongoPersistentProperty(property, owner, simpleTypeHolder) {
                @Override
                Property property() {
                    return property;
                }

                @Override
                Class<?> entityMirrorClass() {
                    return CollectionDescriptorMirror.class;
                }
            };
        }

        return super.createPersistentProperty(property, owner, simpleTypeHolder);
    }

    private static class CustomCollectionMappingMongoPersistentEntity<T> extends BasicMongoPersistentEntity<T> {

        CustomCollectionMappingMongoPersistentEntity(TypeInformation<T> typeInformation) {
            super(typeInformation);
        }

        @Override
        public String getCollection() {
            if (getType() == Descriptor.class) {
                return Descriptor.COLLECTION;
            }
            return super.getCollection();
        }

        @Override
        public <A extends Annotation> A findAnnotation(Class<A> annotationType) {
            return mirrorClass().getAnnotation(annotationType);
        }

        private Class<?> mirrorClass() {
            Class<?> mirrorClass;
            if (getType() == Descriptor.class) {
                mirrorClass = DescriptorMirror.class;
            } else if (getType() == CollectionDescriptor.class) {
                mirrorClass = CollectionDescriptor.class;
            } else {
                throw new IllegalStateException("The following type is not supported: " + getType());
            }
            return mirrorClass;
        }

        @Override
        public <A extends Annotation> boolean isAnnotationPresent(Class<A> annotationType) {
            return findAnnotation(annotationType) != null;
        }
    }

    private abstract class MirroringCachingMongoPersistentProperty extends CachingMongoPersistentProperty {
        public MirroringCachingMongoPersistentProperty(Property property, MongoPersistentEntity<?> owner,
                                                       SimpleTypeHolder simpleTypeHolder) {
            super(property, owner, simpleTypeHolder, CustomDescriptorCollectionMappingContext.this.fieldNamingStrategy);
        }

        @Override
        public <A extends Annotation> A findAnnotation(Class<A> annotationType) {
            Field mirrorField = findDescriptorMirrorField(property());
            return AnnotatedElementUtils.findMergedAnnotation(mirrorField, annotationType);
        }

        private Field findDescriptorMirrorField(Property property) {
            try {
                return entityMirrorClass().getDeclaredField(property.getName());
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(
                        String.format("No '%s' field on '%s'", property.getName(), entityMirrorClass()), e);
            }
        }

        abstract Property property();

        abstract Class<?> entityMirrorClass();

        @Override
        public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
            return findAnnotation(annotationType) != null;
        }
    }

    @Document(Descriptor.COLLECTION)
    private static class DescriptorMirror {
        @Id
        private String externalId;
        private String iri;

        private Descriptor.Type type;
        private Descriptor.Readiness readiness;

        @Indexed(unique = true, sparse = true)
        private String internalId;
        private String modelType;
        private String storageType;

        private CollectionDescriptor collection;

        @CreatedDate
        private ZonedDateTime created;
        @LastModifiedDate
        private ZonedDateTime modified;
        @Version
        private Long version;

        private boolean deleted;

        private Display display;

        @Transient
        private boolean single;

        @Transient
        private Mono<String> externalIdReactively;
        @Transient
        private Mono<String> internalIdReactively;
        @Transient
        private Mono<String> storageTypeReactively;
        private Map<String, Object> stump;

        @Transient
        private Mono<String> modelTypeReactively;

        private OwnedModelDescriptor owned;
    }

    private static class CollectionDescriptorMirror {
        private UUID id;
        private CollectionDescriptor.Type type;
        private CollectionCoordinates coordinates;
        @Indexed(unique = true, sparse = true)
        private String coordinatesString;
    }
}
