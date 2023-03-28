package io.extremum.security.rules.provider;

import io.extremum.security.rules.model.SecurityRule;
import io.extremum.security.rules.model.ServiceType;
import io.extremum.security.rules.parser.ExtremumCELLibrary;
import lombok.SneakyThrows;
import org.projectnessie.cel.tools.Script;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AbstractSecurityRuleProvider implements SecurityRuleProvider {


    protected Map<ServiceType, List<SecurityRule>> composeServiceTypeToSecurityRulesMap(List<String> rulesStringList) {
        final Map<ServiceType, List<SecurityRule>> serviceTypeToSecurityRulesMap;
        serviceTypeToSecurityRulesMap = parseRulesFromString(rulesStringList)
                .stream()
                .collect(
                        Collectors.groupingBy(
                                SecurityRule::getService, Collectors.mapping(securityRule -> securityRule, Collectors.toList())
                        )
                );

        return serviceTypeToSecurityRulesMap;
    }

    protected Map<ServiceType, List<SecurityRule>> serviceTypeToSecurityRulesMap = null;

    protected abstract List<String> getRulesStringsList();

    @Override
    public List<SecurityRule> getRules(ServiceType serviceType) {
        if (serviceTypeToSecurityRulesMap == null) {
            serviceTypeToSecurityRulesMap = composeServiceTypeToSecurityRulesMap(getRulesStringsList());
        }

        return Optional.ofNullable(serviceTypeToSecurityRulesMap.get(serviceType)).orElse(Collections.emptyList());
    }

    @Override
    public List<SecurityRule> getRules(ServiceType serviceType, String modelName) {
        return getRules(serviceType)
                .stream()
                .filter(securityRule -> securityRule.getPattern().matches(Paths.get("/" + modelName.toLowerCase())) || securityRule.getPattern().matches(Paths.get("/**)")))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("SameParameterValue")
    @SneakyThrows
    private List<SecurityRule> parseRulesFromString(List<String> rulesStringList) {
        List<SecurityRule> parsedRules = new ArrayList<>();
        rulesStringList
                .forEach(securityRuleString -> parsedRules.add(parseSecurityRuleString(securityRuleString)));

        return parsedRules;
    }

    @SneakyThrows
    private SecurityRule parseSecurityRuleString(String securityRuleString) {
        Map<String, Object> scriptArgs = new HashMap<>();
        scriptArgs.put("db", SecurityRule.DB());
        scriptArgs.put("storage", SecurityRule.STORAGE());
        scriptArgs.put("iam", SecurityRule.IAM());
        scriptArgs.put("management", SecurityRule.MANAGEMENT());
        scriptArgs.put("functions", SecurityRule.FUNCTIONS());
        scriptArgs.put("signals", SecurityRule.SIGNALS());
        scriptArgs.put("messaging", SecurityRule.MESSAGING());

        scriptArgs.put("context", new Object());
        scriptArgs.put("object", new Object());

        return buildScript(securityRuleString).execute(SecurityRule.class, scriptArgs);
    }

    private Script buildScript(String securityRuleString) {
        return new ExtremumCELLibrary().buildScript(securityRuleString);
    }
}
