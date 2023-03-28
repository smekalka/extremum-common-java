package io.extremum.elasticsearch.springdata.reactiverepository;

import io.extremum.common.annotation.InfrastructureElement;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Import;
import org.springframework.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories;
import org.springframework.data.repository.config.DefaultRepositoryBaseClass;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;

import java.lang.annotation.*;

/**
 * Our custom analogue of @{@link EnableReactiveElasticsearchRepositories}.
 * Our annotation is needed to have our common registrar; this is needed
 * because we need to load packages-to-scan from the configuration, and
 * this cannot be achieved with standard Spring Boot means.
 *
 * NB: value() is overwriten at runtime!
 * XXX: repositoryFactoryBeanClass attribute MUST always be specified!
 * Otherwise, this will not work.
 *
 * @author rpuch
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(ExtremumReactiveElasticsearchRepositoriesRegistrar.class)
@InfrastructureElement
public @interface EnableExtremumReactiveElasticsearchRepositories {

	/**
	 * Alias for the {@link #basePackages()} attribute.
	 */
	String[] value() default {};

	/**
	 * Base packages to scan for annotated components. {@link #value()} is an alias for (and mutually exclusive with) this
	 * attribute. Use {@link #basePackageClasses()} for a type-safe alternative to String-based package names.
	 */
	String[] basePackages() default {};

	/**
	 * Type-safe alternative to {@link #basePackages()} for specifying the packages to scan for annotated components. The
	 * package of each class specified will be scanned. Consider creating a special no-op marker class or interface in
	 * each package that serves no purpose other than being referenced by this attribute.
	 */
	Class<?>[] basePackageClasses() default {};

	/**
	 * Specifies which types are eligible for component scanning. Further narrows the set of candidate components from
	 * everything in {@link #basePackages()} to everything in the base packages that matches the given filter or filters.
	 */
	Filter[] includeFilters() default {};

	/**
	 * Specifies which types are not eligible for component scanning.
	 */
	Filter[] excludeFilters() default {};

	/**
	 * Returns the postfix to be used when looking up custom repository implementations. Defaults to {@literal Impl}. So
	 * for a repository named {@code PersonRepository} the corresponding implementation class will be looked up scanning
	 * for {@code PersonRepositoryImpl}.
	 *
	 * @return
	 */
	String repositoryImplementationPostfix() default "Impl";

	/**
	 * Configures the location of where to find the Spring Data named queries properties file. Will default to
	 * {@code META-INF/elasticsearch-named-queries.properties}.
	 *
	 * @return
	 */
	String namedQueriesLocation() default "";

	/**
	 * Returns the key of the {@link QueryLookupStrategy} to be used for lookup queries for query methods. Defaults to
	 * {@link Key#CREATE_IF_NOT_FOUND}.
	 */
	Key queryLookupStrategy() default Key.CREATE_IF_NOT_FOUND;

	/**
	 * Returns the {@link FactoryBean} class to be used for each repository instance.
	 *
	 * XXX: this has been changed to Object.class to avoid annotation parsing error in
	 * case the application does not have spring-data-jpa in classpath. Hence, the default
	 * is USELESS: repositoryFactoryBeanClass MUST be ALWAYS specified!!!
	 */
	Class<?> repositoryFactoryBeanClass() default Object.class;

	/**
	 * Configure the repository base class to be used to create repository proxies for this particular configuration.
	 */
	Class<?> repositoryBaseClass() default DefaultRepositoryBaseClass.class;

	/**
	 * Configures the name of the {@link org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations} bean
	 * to be used with the repositories detected.
	 */
	String reactiveElasticsearchTemplateRef() default "reactiveElasticsearchTemplate";

	/**
	 * Configures whether nested repository-interfaces (e.g. defined as inner classes) should be discovered by the
	 * repositories infrastructure.
	 */
	boolean considerNestedRepositories() default false;
}
