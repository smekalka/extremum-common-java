package io.extremum.security.rules.provider;

import io.extremum.security.rules.model.SecurityRule;
import io.extremum.security.rules.model.ServiceType;

import java.util.List;

public interface SecurityRuleProvider {

    List<SecurityRule> getRules(ServiceType serviceType);

    List<SecurityRule> getRules(ServiceType serviceType, String modelName);
}
