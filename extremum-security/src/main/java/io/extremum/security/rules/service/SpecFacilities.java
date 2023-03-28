package io.extremum.security.rules.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.expr.v1alpha1.Expr;
import com.jayway.jsonpath.JsonPath;
import io.extremum.common.model.CollectionFilter;
import io.extremum.security.rules.model.SecurityRule;
import io.extremum.security.rules.parser.ExtremumCELLibrary;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.basic.Multilingual;
import io.extremum.sharedmodels.basic.MultilingualLanguage;
import io.extremum.sharedmodels.basic.StringOrMultilingual;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.criteria.internal.PathImplementor;
import org.hibernate.query.criteria.internal.path.SingularAttributePath;
import org.projectnessie.cel.Ast;
import org.projectnessie.cel.Env;
import org.projectnessie.cel.EnvOption;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.criteria.*;
import javax.persistence.metamodel.Attribute;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpecFacilities {

    private static final ExtremumCELLibrary lib = new ExtremumCELLibrary();

    private final Locale locale;

    public SpecFacilities(Locale locale) {
        this.locale = locale;
    }

    public <M> Specification<M> composeSpec(CollectionFilter filter) {
        if (filter.getCelExpr() == null || filter.getCelExpr().isEmpty()) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.and();
        }
        Ast ast = Env.newEnv(lib.getCompileOptions().toArray(new EnvOption[0])).parse(filter.getCelExpr()).getAst();
        if (ast == null) {
            throw new InvalidFilterException(filter);
        }
        List<Specification<M>> specs = Stream.of(ast).map(it -> (Specification<M>) (root, query, criteriaBuilder) -> {
            query.distinct(true);
            Condition condition = composeCondition(filter.getLocale(), ast.getExpr().getCallExpr());
            if (condition.getValue() == null) {
                return criteriaBuilder.and();
            }
            return toPredicate(getRoot(root, condition), criteriaBuilder, it.getExpr(), false, query, condition);
        }).collect(Collectors.toList());
        return conjunctSpecifications(specs);
    }

    public <M> Specification<M> composeSpec(List<SecurityRule> securityRules) {
        List<Specification<M>> specs = securityRules.stream().map(
                securityRule -> (Specification<M>) (root, query, criteriaBuilder) -> {
                    CollectionFilter collectionFilter = new CollectionFilter(securityRule.getExpression(), null);
                    if (securityRule.getAst() == null) {
                        return criteriaBuilder.and();
                    }
                    return toPredicate(
                            root, criteriaBuilder, securityRule.getAst().getExpr(), false, query, composeCondition(collectionFilter.getLocale(), securityRule.getAst().getExpr().getCallExpr())
                    );
                }
        ).collect(Collectors.toList());

        return conjunctSpecifications(specs);
    }

    private static <M> Specification<M> conjunctSpecifications(List<Specification<M>> specs) {
        if (!specs.isEmpty()) {
            Specification<M> spec = specs.get(0);
            for (int i = 1; i < specs.size(); i++) {
                spec = spec.or(specs.get(1));
            }

            return spec;

        } else {
            return (root, query, criteriaBuilder) -> criteriaBuilder.and();
        }
    }

    public static Condition composeCondition(MultilingualLanguage locale, Expr.Call callExpr) {
        return composeCondition(locale, callExpr, "object");
    }

    public static Condition composeCondition(MultilingualLanguage locale, Expr.Call callExpr, String var) {
        List<String> attributeNameChain = new ArrayList<>();
        List<String> attributeValueChain;
        List<Object> args = new ArrayList<>();
        List<String> identExprChain;
        if (!callExpr.getFunction().isEmpty()) {
            identExprChain = getIdentExprChain(callExpr.getTarget());
            if (!identExprChain.isEmpty() && identExprChain.get(0).equals(var)) {
                attributeNameChain = identExprChain.subList(1, identExprChain.size());
            }
        }
        for (Expr expr : callExpr.getArgsList()) {
            identExprChain = getIdentExprChain(expr);
            if (!identExprChain.isEmpty() && identExprChain.get(0).equals(var)) {
                attributeNameChain = identExprChain.subList(1, identExprChain.size());
            }


            if (!identExprChain.isEmpty() && identExprChain.get(0).equals("context")) {
                attributeValueChain = identExprChain.subList(1, identExprChain.size());
                if (attributeValueChain.get(0).equals("user")) {
                    args.add(getValue(attributeValueChain.subList(1, attributeValueChain.size()), SecurityContextHolder.getContext().getAuthentication().getPrincipal()));
                } else {
                    throw new IllegalArgumentException("Only context.user is allowed");
                }
            }

            if (!StringUtils.isBlank(expr.getListExpr().toString())) {
                args = expr.getListExpr().getElementsList().stream().map(SpecFacilities::getConstExprValue).collect(Collectors.toList());
            }
            if (!StringUtils.isBlank(expr.getConstExpr().toString())) {
                args.add(getConstExprValue(expr));
            }
        }

        return new Condition(args, attributeNameChain, locale);
    }

    private static Object getConstExprValue(Expr expr) {
        switch (expr.getConstExpr().getConstantKindCase()) {
            case STRING_VALUE:
                return expr.getConstExpr().getStringValue();
            case DOUBLE_VALUE:
                return expr.getConstExpr().getDoubleValue();
            case INT64_VALUE:
                return expr.getConstExpr().getInt64Value();
            case BOOL_VALUE:
                return expr.getConstExpr().getBoolValue();
        }
        return null;
    }

    private static List<String> getIdentExprChain(Expr expr) {
        List<String> chain = new ArrayList<>();
        walk(chain, expr);
        Collections.reverse(chain);

        return chain;
    }

    private static void walk(List<String> chain, Expr expr) {
        if (!expr.getConstExpr().toString().equals("")) {
            return;
        }
        if (!expr.getIdentExpr().getName().equals("")) {
            chain.add(expr.getIdentExpr().getName());
        } else {
            if (expr.hasSelectExpr()) {
                chain.add(expr.getSelectExpr().getField());
                walk(chain, expr.getSelectExpr().getOperand());
            }
        }
    }

    @SneakyThrows
    private static Object getValue(List<String> valuePath, Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        return JsonPath.read(objectMapper.writeValueAsString(object), "$." + String.join(".", valuePath));
    }

    private static From getRoot(From root, Condition condition) {
        if (condition.forNested()) {
            return root.join(condition.getNested());
        }

        return root;
    }

    @SneakyThrows
    public Predicate toPredicate(From root, CriteriaBuilder criteriaBuilder, Expr expr, boolean negative, CriteriaQuery<?> query, Condition condition) {
        if (expr.hasComprehensionExpr()) {
            return comprehensiveExprToPredicate(root, criteriaBuilder, expr, query, condition, negative);
        }
        if (expr.hasCallExpr()) {
            return callExpressionToPredicate(root, criteriaBuilder, expr, negative, query, condition);
        }

        throw new IllegalArgumentException();
    }

    private Predicate comprehensiveExprToPredicate(From root, CriteriaBuilder criteriaBuilder, Expr expr, CriteriaQuery<?> query, Condition condition, Boolean negative) {
        Expr.Comprehension comprehensionExpr = expr.getComprehensionExpr();
        Expr.Call callExpr = comprehensionExpr.getLoopStep().getCallExpr();
        if ("_||_".equals(callExpr.getFunction())) {
            Expr loopStepCallExprArg = callExpr.getArgsList().get(1);
            Condition comprehensiveCondition = composeCondition(condition.getLocale(), loopStepCallExprArg.getCallExpr(), comprehensionExpr.getIterVar());
            comprehensiveCondition.setNested(comprehensionExpr.getIterRange().getSelectExpr().getField());

            Subquery subquery = query.subquery(getRoot(root, comprehensiveCondition).getJavaType());
            Join<Object, Object> join = subquery.correlate((Root) root).join(comprehensiveCondition.getNested());
            subquery.select(join);
            subquery.where(toPredicate(join, criteriaBuilder, loopStepCallExprArg, false, query, comprehensiveCondition));
            if (negative) {
                return criteriaBuilder.not(criteriaBuilder.exists(subquery));
            }
            return criteriaBuilder.exists(subquery);
        }

        return criteriaBuilder.and();
    }

    private Predicate callExpressionToPredicate(From root, CriteriaBuilder criteriaBuilder, Expr expr, boolean negative, CriteriaQuery<?> query, Condition condition) {
        Expr.Call callExpr = expr.getCallExpr();
        switch (callExpr.getFunction()) {
            case "_&&_": {
                List<Predicate> predicates = new ArrayList<>();
                for (Expr arg : callExpr.getArgsList()) {
                    Condition subCondition = composeCondition(condition.getLocale(), arg.getCallExpr());
                    predicates.add(toPredicate(getRoot(root, subCondition), criteriaBuilder, arg, false, query, subCondition));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }

            case "_||_": {
                List<Predicate> predicates = new ArrayList<>();
                for (Expr arg : callExpr.getArgsList()) {
                    Condition subCondition = composeCondition(condition.getLocale(), arg.getCallExpr());
                    predicates.add(toPredicate(getRoot(root, subCondition), criteriaBuilder, arg, false, query, subCondition));
                }
                return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
            }

            case "_==_":
                return getPredicate(
                        criteriaBuilder, condition, root, path -> {
                            matchConditionWithPath(condition, path);
                            if (path instanceof SingularAttributePath && ((SingularAttributePath) path).getAttribute().getName().equals("id")) {
                                condition.setValue(UUID.fromString(Descriptor.builder().externalId(condition.getValue().toString()).build().getInternalId()));
                            }
                            return criteriaBuilder.equal(
                                    path, condition.getValue()
                            );
                        }
                );

            case "_!=_":
                return getPredicate(
                        criteriaBuilder, condition, root, path -> {
                            matchConditionWithPath(condition, path);
                            return criteriaBuilder.notEqual(
                                    path, condition.getValue()
                            );
                        }
                );

            case "_>_": {

                if (isDate(condition.getValue())) {
                    return criteriaBuilder.greaterThan(root.get(condition.getField()), ZonedDateTime.parse((CharSequence) condition.getValue()));
                }
                return criteriaBuilder.gt(root.get(condition.getField()), (Number) condition.getValue());
            }

            case "_>=_": {
                if (isDate(condition.getValue())) {
                    return criteriaBuilder.greaterThanOrEqualTo(root.get(condition.getField()), ZonedDateTime.parse((CharSequence) condition.getValue()));
                }
                return criteriaBuilder.ge(root.get(condition.getField()), (Number) condition.getValue());
            }

            case "_<_": {
                if (isDate(condition.getValue())) {
                    return criteriaBuilder.lessThan(root.get(condition.getField()), ZonedDateTime.parse((CharSequence) condition.getValue()));
                }
                return criteriaBuilder.lt(root.get(condition.getField()), (Number) condition.getValue());
            }

            case "_<=_": {
                if (isDate(condition.getValue())) {
                    return criteriaBuilder.lessThanOrEqualTo(root.get(condition.getField()), ZonedDateTime.parse((CharSequence) condition.getValue()));
                }
                return criteriaBuilder.le(root.get(condition.getField()), (Number) condition.getValue());
            }

            case "@in":
            case ExtremumCELLibrary.ANY:
                return getPredicate(
                        criteriaBuilder, condition, root, path -> path.in(((ArrayList<?>) condition.getValue()).toArray(new Object[0]))
                );

            case "contains":
            case "exists":
                return criteriaBuilder.and();


            case "like":
                return getPredicate(
                        criteriaBuilder, condition, root, path -> {
                            if (negative) {
                                return criteriaBuilder.notLike(path, ((String) condition.getValue()).replace("*", "%"));
                            } else {
                                return criteriaBuilder.like(path, ((String) condition.getValue()).replace("*", "%"));

                            }
                        });

            case "eq":
                return getPredicate(
                        criteriaBuilder, condition, root, path -> {
                            matchConditionWithPath(condition, path);
                            if (negative) {
                                return criteriaBuilder.notEqual(path, condition.getValue());
                            } else {
                                return criteriaBuilder.equal(path, condition.getValue());
                            }
                        }
                );

            case "!_":
                return toPredicate(root, criteriaBuilder, callExpr.getArgsList().get(0), true, query, condition);
            case "matches":
                return getPredicate(
                        criteriaBuilder, condition, root, path -> {
                            if (negative) {
                                return criteriaBuilder.isFalse(
                                        criteriaBuilder.function(
                                                "fts",
                                                Boolean.class,
                                                criteriaBuilder.literal(locale.getDisplayLanguage().toLowerCase()),
                                                path,
                                                criteriaBuilder.literal(locale.getDisplayLanguage().toLowerCase()),
                                                criteriaBuilder.literal(condition.getValue())
                                        )
                                );
                            } else {
                                return criteriaBuilder.isTrue(
                                        criteriaBuilder.function(
                                                "fts",
                                                Boolean.class,
                                                criteriaBuilder.literal(locale.getDisplayLanguage().toLowerCase()),
                                                path,
                                                criteriaBuilder.literal(locale.getDisplayLanguage().toLowerCase()),
                                                criteriaBuilder.literal(condition.getValue())
                                        )
                                );
                            }
                        }
                );

            default:
                throw new IllegalArgumentException(callExpr.getFunction() + " is not allowed in security expressions");
        }
    }

    private void matchConditionWithPath(Condition condition, Path path) {
        if (path instanceof SingularAttributePath) {
            Class bindableJavaType = ((SingularAttributePath) path).getAttribute().getBindableJavaType();
            if (bindableJavaType.isEnum()) {
                condition.setValue(Enum.valueOf(bindableJavaType, (String) condition.getValue()));
            }
        }
    }

    private boolean isDate(Object value) {
        try {
            ZonedDateTime.parse((CharSequence) value);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private static <M> Predicate getPredicate(CriteriaBuilder criteriaBuilder, Condition condition, From<M, Object> root, Function<Path, Predicate> function) {
        if (isStringOrMultilingual(condition, root)) {
            Join<Model, StringOrMultilingual> stringOrMultilingualJoin = root.join(condition.getField());
            Path<?> textPath = stringOrMultilingualJoin.get("text");
            if (condition.localized()) {
                MapJoin<Multilingual, MultilingualLanguage, String> content = getContent(stringOrMultilingualJoin);
                Predicate multilingualPredicate;
                if (condition.isAnyLocale()) {
                    multilingualPredicate = criteriaBuilder.and(function.apply(content.value()));
                } else {
                    multilingualPredicate = criteriaBuilder.and(
                            function.apply(content.value()),
                            criteriaBuilder.equal(content.key(), condition.getLocale()));

                }

                return criteriaBuilder.or(multilingualPredicate);

            } else {
                return function.apply(textPath);
            }
        } else {
            if (((PathImplementor<?>) root).getAttribute() != null && ((PathImplementor<?>) root).getAttribute().getPersistentAttributeType() == Attribute.PersistentAttributeType.ELEMENT_COLLECTION) {
                if (condition.getField() != null) {
                    return function.apply(getRoot(root, condition).get(condition.getField()));
                } else {
                    function.apply(getRoot(root, condition));
                }
            }
            return function.apply(root.get(condition.getField()));
        }
    }

    private static <M> MapJoin<Multilingual, MultilingualLanguage, M> getContent(Join<Model, StringOrMultilingual> stringOrMultilingualJoin) {
        return stringOrMultilingualJoin.join("multilingualContent", JoinType.LEFT).joinMap("map", JoinType.LEFT);
    }

    private static <M> boolean isStringOrMultilingual(Condition condition, From<M, Object> root) {
        boolean isStringOrMultilingual;
        if (condition.getField() == null) {
            return false;
        }
        try {
            isStringOrMultilingual = root.getModel().getBindableJavaType().getDeclaredField(condition.getField()).getType().equals(StringOrMultilingual.class);
        } catch (NoSuchFieldException e) {
            isStringOrMultilingual = false;
        }
        return isStringOrMultilingual;
    }
}