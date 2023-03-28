package io.extremum.everything.services.defaultservices;

import io.extremum.common.model.CollectionFilter;
import io.extremum.common.service.CommonService;
import io.extremum.common.support.CommonServices;
import io.extremum.common.support.ModelClasses;
import io.extremum.everything.support.ModelDescriptors;
import io.extremum.security.rules.model.AllowScope;
import io.extremum.security.rules.model.SecurityRule;
import io.extremum.security.rules.model.ServiceType;
import io.extremum.security.rules.provider.SecurityRuleProvider;
import io.extremum.security.rules.service.InvalidFilterException;
import io.extremum.security.rules.service.SpecFacilities;
import io.extremum.sharedmodels.basic.Model;
import org.hibernate.query.criteria.internal.BasicPathUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.stream.Collectors;

public class SecurityRulesGetterViaCommonServices extends DefaultGetterViaCommonServices {

    private final SpecFacilities specFacilities;

    public SecurityRulesGetterViaCommonServices(CommonServices commonServices, ModelDescriptors modelDescriptors, ModelClasses modelClasses, SecurityRuleProvider securityRuleProvider, SpecFacilities specFacilities) {
        super(commonServices, modelDescriptors, modelClasses);
        this.securityRuleProvider = securityRuleProvider;
        this.specFacilities = specFacilities;
    }

    private final SecurityRuleProvider securityRuleProvider;

    @Override
    public Page<Model> getAll(String modelName, Pageable pageable) {
        CommonService<Model> service = findServiceByModelName(modelName);
        List<SecurityRule> rules = securityRuleProvider.getRules(ServiceType.DB, modelName).stream().filter(securityRule -> securityRule.getAllow().contains(AllowScope.READ)).collect(Collectors.toList());
        Specification<Model> securitySpec = specFacilities.composeSpec(rules);

        return service.findAll(pageable, securitySpec);
    }

    @Override
    public Page<Model> getAll(String modelName, CollectionFilter filter, Pageable pageable) {
        CommonService<Model> service = findServiceByModelName(modelName);
        List<SecurityRule> rules = securityRuleProvider.getRules(ServiceType.DB, modelName).stream().filter(securityRule -> securityRule.getAllow().contains(AllowScope.READ)).collect(Collectors.toList());
        Specification<Model> securitySpec = specFacilities.composeSpec(rules);
        Specification<Model> filterSpec;
        try {
            filterSpec = specFacilities.composeSpec(filter);
        } catch (RuntimeException e) {
            throw new InvalidFilterException(filter);
        }
        filterSpec = filterSpec.and(securitySpec);
        Page<Model> all;

        try {
            all = service.findAll(pageable, filterSpec);
        } catch (BasicPathUsageException e) {
            throw new InvalidFilterException(filter);
        }

        return all;
    }
}