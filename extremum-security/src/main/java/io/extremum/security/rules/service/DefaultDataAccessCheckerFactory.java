package io.extremum.security.rules.service;

import io.extremum.security.rules.provider.SecurityRuleProvider;
import io.extremum.security.services.DataAccessChecker;
import io.extremum.sharedmodels.basic.Model;

public class DefaultDataAccessCheckerFactory implements DataAccessCheckerFactory {

    @Override
    public <M extends Model> DataAccessChecker<M> createSecurityRuleDataAccessChecker(String supportedModel, SecurityRuleProvider securityRuleProvider) {
        return new SecurityRulesDataAccessChecker<>(supportedModel, securityRuleProvider);
    }
}