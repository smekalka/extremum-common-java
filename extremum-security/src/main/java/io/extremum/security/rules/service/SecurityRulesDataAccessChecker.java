package io.extremum.security.rules.service;

import io.extremum.common.annotation.ModelName;
import io.extremum.security.CheckerContext;
import io.extremum.security.model.Context;
import io.extremum.security.rules.model.AllowScope;
import io.extremum.security.rules.model.SecurityRule;
import io.extremum.security.rules.model.ServiceType;
import io.extremum.security.rules.provider.SecurityRuleProvider;
import io.extremum.security.services.DataAccessChecker;
import io.extremum.sharedmodels.basic.Model;
import lombok.SneakyThrows;
import org.projectnessie.cel.tools.Script;
import org.projectnessie.cel.tools.ScriptException;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SecurityRulesDataAccessChecker<M extends Model> implements DataAccessChecker<M> {

    public SecurityRulesDataAccessChecker(
            String supportedModel, SecurityRuleProvider securityRuleProvider) {
        this.supportedModel = supportedModel;
        this.securityRuleProvider = securityRuleProvider;
    }

    private final String supportedModel;
    private final SecurityRuleProvider securityRuleProvider;

    @Override
    public String getSupportedModel() {
        return supportedModel;
    }

    @Override
    @SneakyThrows
    public boolean allowedToGet(Model model, CheckerContext context) {
        return checkAccess(model, context, AllowScope.READ);
    }

    private boolean checkAccess(Model model, CheckerContext context, AllowScope allowScope) throws ScriptException {
        if (!context.getCurrentPrincipal().isPresent()) {
            return false;
        }
        List<SecurityRule> rules = securityRuleProvider.getRules(ServiceType.DB, getModelName(model));
        return checkAccess(model, context, rules, allowScope);
    }


    @Override
    @SneakyThrows
    public boolean allowedToPatch(Model model, CheckerContext context) {
        return checkAccess(model, context, AllowScope.UPDATE);
    }

    @Override
    @SneakyThrows
    public boolean allowedToRemove(Model model, CheckerContext context) {
        return checkAccess(model, context, AllowScope.DELETE);
    }

    @Override
    @SneakyThrows
    public boolean allowedToWatch(Model model, CheckerContext context) {
        return checkAccess(model, context, AllowScope.WATCH);
    }

    @Override
    @SneakyThrows
    public boolean allowedToCreate(Model model, CheckerContext context) {
        return checkAccess(model, context, AllowScope.CREATE);
    }

    private String getModelName(Model model) {
        String modelName;
        ModelName annotation = model.getClass().getAnnotation(ModelName.class);
        if (annotation != null) {
            modelName = annotation.value();
        } else {
            modelName = model.getClass().getSimpleName();
        }

        return modelName;
    }

    private boolean checkAccess(Model model, CheckerContext context, List<SecurityRule> rules, AllowScope allowScope) throws ScriptException {
        List<SecurityRule> readRules = rules.stream().filter(securityRule -> securityRule.getAllow().contains(allowScope)).collect(Collectors.toList());
        Map<String, Object> scriptArgs = new HashMap<>();
        scriptArgs.put("db", SecurityRule.DB());
        scriptArgs.put("blob", SecurityRule.STORAGE());
        Principal principal = context.getCurrentPrincipal().orElseThrow(() -> new IllegalStateException("The principal is not present"));
        scriptArgs.put("context", new Context(principal));
        scriptArgs.put("object", model);

        for (SecurityRule readRule : readRules) {
            Script accessChecker = readRule.getAccessChecker();
            if (accessChecker == null) {
                return true;
            }
            Boolean allowed = accessChecker.execute(Boolean.class, scriptArgs);
            if (allowed) {
                return true;
            }
        }

        return false;
    }

}
