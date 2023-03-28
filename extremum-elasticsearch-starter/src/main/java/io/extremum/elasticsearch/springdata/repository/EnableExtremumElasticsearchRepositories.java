package io.extremum.elasticsearch.springdata.repository;

import io.extremum.common.annotation.InfrastructureElement;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Import;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.repository.config.DefaultRepositoryBaseClass;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Our custom analogue of @{@link EnableElasticsearchRepositories}.
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
@Import(ExtremumElasticsearchRepositoriesRegistrar.class)
@InfrastructureElement
public @interface EnableExtremumElasticsearchRepositories {
	/**
	 * Alias for the {@link #basePackages()} attribute. Allows for more concise annotation declarations e.g.:
	 * {@code @EnableElasticsearchRepositories("org.my.pkg")} instead of
	 * {@code @EnableElasticsearchRepositories(basePackages="org.my.pkg")}.
	 */
	String[] value() default {};

	/**
	 * Base packages to scan for annotated components. {@link #value()} is an alias for (and mutually exclusive with) this
	 * attribute. Use {@link #basePackageClasses()} for a type-safe alternative to text-based package names.
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
	 * {@code META-INFO/elasticsearch-named-queries.properties}.
	 *
	 * @return
	 */
	String namedQueriesLocation() default "";

	/**
	 * Returns the key of the {@link org.springframework.data.repository.query.QueryLookupStrategy} to be used for lookup
	 * queries for query methods. Defaults to
	 * {@link Key#CREATE_IF_NOT_FOUND}.
	 *
	 * @return
	 */
	Key queryLookupStrategy() default Key.CREATE_IF_NOT_FOUND;

	/**
	 * Returns the {@link org.springframework.beans.factory.FactoryBean} class to be used for each repository instance.
	 *
	 * XXX: this has been changed to Object.class to avoid annotation parsing error in
	 * case the application does not have spring-data-jpa in classpath. Hence, the default
	 * is USELESS: repositoryFactoryBeanClass MUST be ALWAYS specified!!!
	 *
	 * @return
	 */
	Class<?> repositoryFactoryBeanClass() default Object.class;

	/**
	 * Configure the repository base class to be used to create repository proxies for this particular configuration.
	 *
	 * @return
	 */
	Class<?> repositoryBaseClass() default DefaultRepositoryBaseClass.class;

	// Elasticsearch specific configuration

	/**
	 * Configures the name of the {@link ElasticsearchRestTemplate} bean definition to be used to create repositories
	 * discovered through this annotation. Defaults to {@code elasticsearchTemplate}.
	 *
	 * @return
	 */
	String elasticsearchTemplateRef() default "elasticsearchTemplate";

	/**
	 * Configures whether nested repository-interfaces (e.g. defined as inner classes) should be discovered by the
	 * repositories infrastructure.
	 */
	boolean considerNestedRepositories() default false;
}
