package io.extremum.mongo.springdata;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.auditing.IsNewAwareAuditingHandler;
import org.springframework.data.auditing.ReactiveIsNewAwareAuditingHandler;
import org.springframework.data.auditing.config.AuditingBeanDefinitionRegistrarSupport;
import org.springframework.data.auditing.config.AuditingConfiguration;
import org.springframework.data.config.ParsingUtils;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.data.mongodb.core.mapping.event.AuditingEntityCallback;
import org.springframework.data.mongodb.core.mapping.event.ReactiveAuditingEntityCallback;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is essentially MongoAuditingRegistrar with one customization. We cannot extend it as it is package local,
 * so we had to copy.
 * The customization is that it applies the auditing to all
 * {@link org.springframework.data.mongodb.core.mapping.MongoMappingContext}s available in the application context,
 * and not only to one. This allows to solve a nasty problem with ZonedDateTime used as created/modified field type.
 * The customization lies entirely in PersistentEntitiesLookup.
 *
 * @author Thomas Darimont
 * @author Oliver Gierke
 * @author rpuch
 */
public class AllMongoAuditingRegistrar extends AuditingBeanDefinitionRegistrarSupport {

    private static final boolean PROJECT_REACTOR_AVAILABLE = ClassUtils.isPresent("reactor.core.publisher.Mono",
            AllMongoAuditingRegistrar.class.getClassLoader());

    /*
     * (non-Javadoc)
     * @see org.springframework.data.auditing.config.AuditingBeanDefinitionRegistrarSupport#getAnnotation()
     */
    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return EnableAllMongoAuditing.class;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.auditing.config.AuditingBeanDefinitionRegistrarSupport#getAuditingHandlerBeanName()
     */
    @Override
    protected String getAuditingHandlerBeanName() {
        return "mongoAuditingHandler";
    }

    protected String getReactiveAuditingHandlerBeanName() {
        return "mongoReactiveAuditingHandler";
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.auditing.config.AuditingBeanDefinitionRegistrarSupport#registerBeanDefinitions(org.springframework.core.type.AnnotationMetadata, org.springframework.beans.factory.support.BeanDefinitionRegistry)
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {

        Assert.notNull(annotationMetadata, "AnnotationMetadata must not be null!");
        Assert.notNull(registry, "BeanDefinitionRegistry must not be null!");

        super.registerBeanDefinitions(annotationMetadata, registry);
    }

    private AuditingConfiguration configuration;
    /*
     * (non-Javadoc)
     * @see org.springframework.data.auditing.config.AuditingBeanDefinitionRegistrarSupport#getAuditHandlerBeanDefinitionBuilder(org.springframework.data.auditing.config.AuditingConfiguration)
     */
    @Override
    protected BeanDefinitionBuilder getAuditHandlerBeanDefinitionBuilder(AuditingConfiguration configuration) {
        this.configuration = configuration;
        Assert.notNull(configuration, "AuditingConfiguration must not be null!");

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(IsNewAwareAuditingHandler.class);

        BeanDefinitionBuilder definition = BeanDefinitionBuilder.genericBeanDefinition(
                PersistentEntitiesLookup.class);
        definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);

        builder.addConstructorArgValue(definition.getBeanDefinition());
        return configureDefaultAuditHandlerAttributes(configuration, builder);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.auditing.config.AuditingBeanDefinitionRegistrarSupport#registerAuditListener(org.springframework.beans.factory.config.BeanDefinition, org.springframework.beans.factory.support.BeanDefinitionRegistry)
     */
    @Override
    protected void registerAuditListenerBeanDefinition(BeanDefinition auditingHandlerDefinition,
            BeanDefinitionRegistry registry) {

        Assert.notNull(auditingHandlerDefinition, "BeanDefinition must not be null!");
        Assert.notNull(registry, "BeanDefinitionRegistry must not be null!");

        BeanDefinitionBuilder listenerBeanDefinitionBuilder = BeanDefinitionBuilder
                .rootBeanDefinition(AuditingEntityCallback.class);
        listenerBeanDefinitionBuilder
                .addConstructorArgValue(
                        ParsingUtils.getObjectFactoryBeanDefinition(getAuditingHandlerBeanName(), registry));

        registerInfrastructureBeanWithId(listenerBeanDefinitionBuilder.getBeanDefinition(),
                AuditingEntityCallback.class.getName(), registry);

        if (PROJECT_REACTOR_AVAILABLE) {
            createAndRegisterReactiveAuditingHandlerBean(registry);
            registerReactiveAuditingEntityCallback(registry, auditingHandlerDefinition.getSource());
        }
    }

    private void createAndRegisterReactiveAuditingHandlerBean(BeanDefinitionRegistry registry) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(ReactiveIsNewAwareAuditingHandler.class);
        BeanDefinitionBuilder definition = BeanDefinitionBuilder.genericBeanDefinition(
                PersistentEntitiesLookup.class);
        definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);
        builder.addConstructorArgValue(definition.getBeanDefinition());
        BeanDefinitionBuilder beanDefinitionBuilder = configureDefaultAuditHandlerAttributes(configuration, builder);
        registry.registerBeanDefinition(getReactiveAuditingHandlerBeanName(), beanDefinitionBuilder.getBeanDefinition());
    }

    private void registerReactiveAuditingEntityCallback(BeanDefinitionRegistry registry, Object source) {

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(ReactiveAuditingEntityCallback.class);

        builder.addConstructorArgValue(
                ParsingUtils.getObjectFactoryBeanDefinition(getReactiveAuditingHandlerBeanName(), registry));
        builder.getRawBeanDefinition().setSource(source);

        registerInfrastructureBeanWithId(builder.getBeanDefinition(), ReactiveAuditingEntityCallback.class.getName(),
                registry);
    }

    /**
     * Simple helper to be able to wire the {@link PersistentEntities} from {@link MappingMongoConverter}s beans
     * available in the application context.
     */
    static class PersistentEntitiesLookup implements FactoryBean<PersistentEntities> {

        private final List<MappingMongoConverter> converters;

        /**
         * Creates a new {@link PersistentEntitiesLookup} for the given {@link MappingMongoConverter}s.
         *
         * @param converters must not be {@literal null}.
         */
        public PersistentEntitiesLookup(List<MappingMongoConverter> converters) {
            this.converters = new ArrayList<>(converters);
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.beans.factory.FactoryBean#getObject()
         */
        @Override
        public PersistentEntities getObject() {
            List<MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty>> contexts = converters
                    .stream()
                    .map(MappingMongoConverter::getMappingContext)
                    .collect(Collectors.toList());
            return new PersistentEntities(contexts);
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.beans.factory.FactoryBean#getObjectType()
         */
        @Override
        public Class<?> getObjectType() {
            return PersistentEntities.class;
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.beans.factory.FactoryBean#isSingleton()
         */
        @Override
        public boolean isSingleton() {
            return true;
        }
    }
}
