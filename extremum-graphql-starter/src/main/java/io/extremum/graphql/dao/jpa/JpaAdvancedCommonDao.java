package io.extremum.graphql.dao.jpa;

import com.google.common.base.CaseFormat;
import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.common.model.CollectionFilter;
import io.extremum.common.spring.data.OffsetBasedPageRequest;
import io.extremum.common.support.ModelClasses;
import io.extremum.everything.services.management.ModelRetriever;
import io.extremum.everything.services.management.ModelSaver;
import io.extremum.graphql.dao.AdvancedCommonDao;
import io.extremum.graphql.dao.IdToModelResolver;
import io.extremum.graphql.model.PagingAndSoringRequestValidator;
import io.extremum.graphql.model.PagingAndSortingRequest;
import io.extremum.security.rules.parser.ExtremumCELLibrary;
import io.extremum.security.rules.service.SpecFacilities;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.SneakyThrows;
import org.hibernate.proxy.HibernateProxy;
import org.jetbrains.annotations.NotNull;
import org.projectnessie.cel.Ast;
import org.projectnessie.cel.Env;
import org.projectnessie.cel.EnvOption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.extremum.security.rules.service.SpecFacilities.composeCondition;

@Transactional
public class JpaAdvancedCommonDao implements AdvancedCommonDao {

    private final ModelRetriever modelRetriever;
    private final ModelSaver modelSaver;
    private final ModelClasses modelClasses;
    private final AdvancedQueryBuilder queryBuilder;
    private final EntityManager entityManager;
    private final SpecFacilities specFacilities;
    private final PagingAndSoringRequestValidator pagingValidator = new PagingAndSoringRequestValidator();
    private final IdToModelResolver idToModelResolver;

    public JpaAdvancedCommonDao(ModelRetriever modelRetriever, ModelSaver modelSaver, ModelClasses modelClasses, AdvancedQueryBuilder queryBuilder, EntityManager entityManager, SpecFacilities specFacilities) {
        this.modelRetriever = modelRetriever;
        this.modelSaver = modelSaver;
        this.modelClasses = modelClasses;
        this.queryBuilder = queryBuilder;
        this.entityManager = entityManager;
        this.specFacilities = specFacilities;
        this.idToModelResolver = new IdToModelResolver(modelRetriever);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Folder extends BasicModel<?>, Item> List<Item> getNestedCollection(Folder model, Class<Item> nestedClass, String collectionName, PagingAndSortingRequest paging) {
        PagingAndSoringRequestValidator pagingValidator = new PagingAndSoringRequestValidator();
        pagingValidator.validate(paging, nestedClass);
        List<Object[]> collectionsUUIDs = queryBuilder.composeQueryForCollectionIds(model, nestedClass, collectionName, paging.getPageable()).getResultList();
        return getItems(paging, collectionsUUIDs);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Folder extends BasicModel<?>, Item> List<Item> getNestedCollection(Folder model, Class<Item> nestedClass, String collectionName, PagingAndSortingRequest paging, String joinTable, String reverseId) {
        PagingAndSoringRequestValidator pagingValidator = new PagingAndSoringRequestValidator();
        pagingValidator.validate(paging, nestedClass);
        List<Object[]> collectionsUUIDs = queryBuilder.composeQueryForCollectionIds(model, nestedClass, collectionName, paging.getPageable(), joinTable, reverseId).getResultList();
        return getItems(paging, collectionsUUIDs);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    private <Item> List<Item> getItems(PagingAndSortingRequest paging, List<Object[]> collectionsUUIDs) {
        List<Item> objects;
        if (collectionsUUIDs.isEmpty()) {
            objects = Collections.emptyList();
        } else {
            if (collectionsUUIDs.get(0)[0] instanceof String) {
                try {
                    UUID.fromString((String) collectionsUUIDs.get(0)[0]);
                    objects = retrieveByIds(paging.getComparator(), getDescriptors(collectionsUUIDs));

                } catch (RuntimeException e) {
                    objects = collectionsUUIDs.stream().map(o -> (Item) o[0]).collect(Collectors.toList());
                }

            } else {
                objects = collectionsUUIDs.stream().map(o -> (Item) o[0]).collect(Collectors.toList());
            }
        }

        return objects;
    }

    @Override
    @SuppressWarnings("unchecked")

    public <Folder extends BasicModel<?>, Item> Page<Item> getNestedCollection(Folder model, CollectionFilter collectionFilter, String collectionName, PagingAndSortingRequest paging) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<?> cq = criteriaBuilder.createQuery(model.getClass());
        Root<?> modelRoot = cq.from(getClass(model));

        Join nestedJoin = modelRoot.join(collectionName);
        Predicate id = criteriaBuilder.equal(nestedJoin.getParentPath().get("id"), UUID.fromString(model.getUuid().getInternalId()));
        ExtremumCELLibrary lib = new ExtremumCELLibrary();
        Predicate predicate = null;
        if (collectionFilter.getCelExpr() != null && !collectionFilter.getCelExpr().isEmpty()) {
            Ast ast = Env.newEnv(lib.getCompileOptions().toArray(new EnvOption[0])).parse(collectionFilter.getCelExpr()).getAst();
            predicate = specFacilities.toPredicate(nestedJoin, criteriaBuilder, ast.getExpr(), false, criteriaBuilder.createQuery(), composeCondition(collectionFilter.getLocale(), ast.getExpr().getCallExpr()));
        }
        CriteriaQuery<?> criteriaQuery;
        if (predicate != null) {
            criteriaQuery = cq.select(nestedJoin).where(criteriaBuilder.and(id, predicate));
        } else {
            criteriaQuery = cq.select(nestedJoin).where(criteriaBuilder.and(id));
        }


        criteriaQuery.distinct(true);
        List<Item> resultList = (List<Item>) entityManager.createQuery(criteriaQuery).setFirstResult(paging.getOffset()).setMaxResults(paging.getLimit()).getResultList();

        Expression count = criteriaBuilder.count(modelRoot);
        CriteriaQuery where = cq.select(count).where(criteriaQuery.getRestriction());
        Object singleResult = entityManager.createQuery(where).getSingleResult();

        return new PageImpl<>(resultList, paging.getPageable(), (Long) singleResult);
    }

    private static Class<?> getClass(Object proxy) {
        if (proxy instanceof HibernateProxy) {
            return ((HibernateProxy) proxy).getHibernateLazyInitializer()
                    .getImplementation()
                    .getClass();
        } else {
            return proxy.getClass();
        }
    }

    @NotNull
    private List<Descriptor> getDescriptors(List<Object[]> collectionsUUIDs) {
        return collectionsUUIDs.stream().map(objects -> Descriptor.builder().internalId((String) objects[0]).build()).collect(Collectors.toList());
    }

    @NotNull
    private <Item> List<Item> retrieveByIds(Comparator<Item> comparator, List<Descriptor> collect) {
        return modelRetriever.retrieveModelByIds(collect).stream().map(m -> (Item) m).sorted(comparator).collect(Collectors.toList());
    }

    @Override
    public <Folder extends BasicModel<?>, Item> void addToNestedCollection(Folder model, List<Item> input, String collectionName) {
        if (input.isEmpty()) {
            return;
        }

        if (input.get(0) instanceof BasicModel<?>) {
            input.forEach(item -> idToModelResolver.resolveNestedModels((BasicModel<?>) item));
            List<Descriptor> savedDescriptor = input.stream().map(
                    role -> {
                        if (((BasicModel<?>) role).getUuid() != null) {
                            return ((BasicModel<?>) role).getUuid();
                        } else {
                            return ((BasicModel<?>) modelSaver.saveModel(((BasicModel<?>) role))).getUuid();
                        }
                    }).collect(Collectors.toList());
            String nestedTable = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, input.get(0).getClass().getSimpleName());
            savedDescriptor.forEach(d -> queryBuilder.insertToNestedModelCollectionQuery(model, nestedTable, d).executeUpdate());
        } else {
            //consider that input is mapped as @ElementCollection

            input.forEach(item -> {
                persist(item);
                queryBuilder.insertToNestedElementCollectionQuery(model, collectionName, item).executeUpdate();
            });
        }

        modelSaver.saveModel(model);
    }

    @SneakyThrows
    private <Item> void persist(Item item){
        Field[] declaredFields = item.getClass().getDeclaredFields();
        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            Object o = declaredField.get(item);
            if(o instanceof BasicModel){
                if(((BasicModel)o).getUuid()==null) {
                    Model model = modelSaver.saveModel((Model) o);
                    declaredField.set(item, model);
                } else {
                    Descriptor uuid = ((BasicModel) o).getUuid();
                    Model model = modelRetriever.retrieveModel(uuid);
                    declaredField.set(item, model);
                }
            }
        }
    }

    @Override
    public <Folder extends BasicModel<?>, Item> void removeFromNestedCollection(Folder model, List<Item> input, String collectionName) {
        if (input.isEmpty()) {
            return;
        }

        if (input.get(0) instanceof BasicModel<?>) {
            input.forEach(item -> queryBuilder.deleteNestedModelCollectionQuery(collectionName, model, (BasicModel<?>) item).executeUpdate());
        } else {
            input.forEach(item -> queryBuilder.deleteNestedElementCollectionQuery(collectionName, model, item).executeUpdate());
        }

        modelSaver.saveModel(model);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Item extends BasicModel<?>> Page<Item> findAll(String modelName, PagingAndSortingRequest paging) {
        PageRequest pageRequest = getPageRequest(modelName, paging);

        return modelRetriever.retrieveModelPage(modelName, pageRequest).map(model -> (Item) model);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Item extends BasicModel<?>> Page<Item> findAll(String modelName, CollectionFilter filter, PagingAndSortingRequest paging) {
        PageRequest pageRequest = getPageRequest(modelName, paging);

        return modelRetriever.retrieveModelPage(modelName, filter, pageRequest).map(model -> (Item) model);
    }

    @NotNull
    private PageRequest getPageRequest(String modelName, PagingAndSortingRequest paging) {
        pagingValidator.validate(paging, modelClasses.getClassByModelName(modelName));
        Pageable pageable = new OffsetBasedPageRequest(paging.getOffset(), paging.getLimit());

        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(
                        paging
                                .getOrders()
                                .stream()
                                .map(sortOrder -> new Sort.Order(sortOrder.getDirection(), sortOrder.getProperty()))
                                .collect(Collectors.toList())
                )
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Item extends BasicModel<?>> Item get(String id) {
        return (Item) modelRetriever.retrieveModel(new Descriptor(id));
    }
}