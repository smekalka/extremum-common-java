package io.extremum.security.rules.service;

import io.extremum.security.rules.model.AllowScope;
import io.extremum.security.rules.model.SecurityRule;
import io.extremum.security.rules.model.ServiceType;
import io.extremum.security.rules.provider.AbstractSecurityRuleProvider;
import io.extremum.security.rules.provider.SecurityRuleProvider;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbstractSecurityRuleProviderTest {

    SecurityRuleProvider securityRuleProvider = new AbstractSecurityRuleProvider() {
        @Override
        public List<String> getRulesStringsList() {
            return Arrays.asList(
                    "db" +
                            ".path('/TestModel')" +
                            ".when('Name' == context.user.name)" +
                            ".allow(['read', 'create'])",
                    "db" +
                            ".path('/TestModel2')" +
                            ".when('Name' == context.user.name)" +
                            ".allow(['read', 'create'])",
                    "storage" +
                            ".path('/path')" +
                            ".allow(['read', 'update', 'delete', 'create'])"

            );
        }
    };

    @Test
    void should_get_rules_for_service_type_properly() {
        List<SecurityRule> rules = securityRuleProvider.getRules(ServiceType.DB);
        assertEquals(2, rules.size());
        SecurityRule securityRule = rules.get(0);
        assertEquals(Arrays.asList(AllowScope.READ, AllowScope.CREATE), securityRule.getAllow());
        assertTrue(securityRule.getPattern().matches(Paths.get("/testmodel")));
        assertEquals("\"Name\" == context.user.name", securityRule.getExpression());
        assertEquals(ServiceType.DB, securityRule.getService());


        rules = securityRuleProvider.getRules(ServiceType.STORAGE);
        assertEquals(1, rules.size());
        assertEquals(1, rules.size());
        securityRule = rules.get(0);
        assertEquals(Arrays.asList(AllowScope.READ, AllowScope.UPDATE, AllowScope.DELETE, AllowScope.CREATE), securityRule.getAllow());
        assertTrue(securityRule.getPattern().matches(Paths.get("/path")));
        assertNull(securityRule.getExpression());
        assertEquals(ServiceType.STORAGE, securityRule.getService());
    }

    @Test
    void should_get_rules_for_service_type_and_model_properly() {
        List<SecurityRule> rules = securityRuleProvider.getRules(ServiceType.DB, "TestModel");
        assertEquals(1, rules.size());
    }
}