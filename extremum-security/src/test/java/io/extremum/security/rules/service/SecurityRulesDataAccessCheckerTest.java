package io.extremum.security.rules.service;

import io.extremum.security.CheckerContext;
import io.extremum.security.rules.provider.AbstractSecurityRuleProvider;
import io.extremum.security.rules.provider.SecurityRuleProvider;
import io.extremum.sharedmodels.auth.User;
import io.extremum.sharedmodels.basic.Model;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityRulesDataAccessCheckerTest {
    private final SecurityRuleProvider securityRuleProvider = new AbstractSecurityRuleProvider() {
        public static final String RULE_1 = "db" +
                ".path('/TestModel')" +
                ".when(object.testModel.owner == context.user.name)" +
                ".allow(['read', 'update', 'delete', 'create'])";
        public static final String RULE_2 = "db" +
                ".path('/OtherModel')" +
                ".allow(['read','create'])";
        public static final String RULE_3 = "db" +
                ".path('/OtherModel')" +
                ".when(context.user.roles.contains('admin'))" +
                ".allow(['delete'])";

        @Override
        public List<String> getRulesStringsList() {
            return Arrays.asList(RULE_1, RULE_2, RULE_3);
        }
    };

    private final SecurityRuleProvider allowAllOtherModelSecurityRuleProvider = new AbstractSecurityRuleProvider() {
        public static final String RULE_1 = "db" +
                ".path('/OtherModel')" +
                ".allow(['read','create','delete','watch','update'])";

        @Override
        public List<String> getRulesStringsList() {
            return Collections.singletonList(RULE_1);
        }
    };

    private final SecurityRulesDataAccessChecker<TestModel> testModelSecurityRulesDataAccessChecker = new SecurityRulesDataAccessChecker<>(
            "TestModel", securityRuleProvider
    );

    private final SecurityRulesDataAccessChecker<TestModel> readAndCreateOnlyOtherModelSecurityRulesDataAccessChecker = new SecurityRulesDataAccessChecker<>(
            "OtherModel", securityRuleProvider
    );

    private final SecurityRulesDataAccessChecker<TestModel> allowAllOtherModelSecurityRulesDataAccessChecker = new SecurityRulesDataAccessChecker<>(
            "OtherModel", allowAllOtherModelSecurityRuleProvider
    );


    private final CheckerContext checkerContext = new CheckerContext() {
        @Override
        public Optional<Principal> getCurrentPrincipal() {
            User user = new User("Name2", "user@example.com", Collections.singletonList("user"));
            return Optional.of(user);
        }

        @Override
        public boolean currentUserHasOneOf(String... roles) {
            return Arrays.asList(roles).contains("user");
        }
    };

    private final CheckerContext otherCheckerContext = new CheckerContext() {
        @Override
        public Optional<Principal> getCurrentPrincipal() {
            User user = new User("OtherName", "user@example.com", Collections.singletonList("admin"));
            return Optional.of(user);
        }

        @Override
        public boolean currentUserHasOneOf(String... roles) {
            return Arrays.asList(roles).contains("user");
        }
    };

    @Test
    void should_get_supported_model() {
        assertEquals("TestModel", testModelSecurityRulesDataAccessChecker.getSupportedModel());
    }

    @Test
    void should_allow_to_get_when_context_username_matches_with_principal_name() {
        assertTrue(testModelSecurityRulesDataAccessChecker.allowedToGet(new TestModel("Name", new TestModel("Name2", new TestModel("Name3", null))), checkerContext));
    }

    @Test
    void should_not_allow_any_when_context_username_matches_with_principal_name() {
        assertFalse(testModelSecurityRulesDataAccessChecker.allowedToGet(new TestModel("Name", new TestModel("Name2", null)), otherCheckerContext));
        assertFalse(testModelSecurityRulesDataAccessChecker.allowedToWatch(new TestModel("Name", new TestModel("Name2", null)), otherCheckerContext));
        assertFalse(testModelSecurityRulesDataAccessChecker.allowedToCreate(new TestModel("Name", new TestModel("Name2", null)), otherCheckerContext));
        assertFalse(testModelSecurityRulesDataAccessChecker.allowedToRemove(new TestModel("Name", new TestModel("Name2", null)), otherCheckerContext));
        assertFalse(testModelSecurityRulesDataAccessChecker.allowedToPatch(new TestModel("Name", new TestModel("Name2", null)), otherCheckerContext));
    }


    @Test
    void should_allow_to_patch_when_context_username_matches_with_principal_name() {
        assertTrue(testModelSecurityRulesDataAccessChecker.allowedToPatch(new TestModel("Name", new TestModel("Name2", null)), checkerContext));
    }

    @Test
    void should_not_allow_to_patch_when_context_username_matches_with_principal_name() {
        assertFalse(testModelSecurityRulesDataAccessChecker.allowedToPatch(new TestModel("Name", new TestModel("Name2", null)), otherCheckerContext));
    }

    @Test
    void should_allow_to_remove_when_context_username_matches_with_principal_name() {
        assertTrue(testModelSecurityRulesDataAccessChecker.allowedToRemove(new TestModel("Name", new TestModel("Name2", null)), checkerContext));
    }

    @Test
    void should_not_allow_to_remove_when_context_username_matches_with_principal_name() {
        assertFalse(testModelSecurityRulesDataAccessChecker.allowedToRemove(new TestModel("Name", new TestModel("Name2", null)), otherCheckerContext));
    }

    @Test
    void should_allow_read_and_create_only() {
        assertTrue(readAndCreateOnlyOtherModelSecurityRulesDataAccessChecker.allowedToGet(new OtherModel(), checkerContext));
        assertFalse(readAndCreateOnlyOtherModelSecurityRulesDataAccessChecker.allowedToRemove(new OtherModel(), checkerContext));
        assertFalse(readAndCreateOnlyOtherModelSecurityRulesDataAccessChecker.allowedToPatch(new OtherModel(), checkerContext));
        assertFalse(readAndCreateOnlyOtherModelSecurityRulesDataAccessChecker.allowedToWatch(new OtherModel(), checkerContext));
        assertTrue(readAndCreateOnlyOtherModelSecurityRulesDataAccessChecker.allowedToCreate(new OtherModel(), checkerContext));
    }

    @Test
    void should_allow_all() {
        assertTrue(allowAllOtherModelSecurityRulesDataAccessChecker.allowedToGet(new OtherModel(), checkerContext));
        assertTrue(allowAllOtherModelSecurityRulesDataAccessChecker.allowedToRemove(new OtherModel(), checkerContext));
        assertTrue(allowAllOtherModelSecurityRulesDataAccessChecker.allowedToPatch(new OtherModel(), checkerContext));
        assertTrue(allowAllOtherModelSecurityRulesDataAccessChecker.allowedToWatch(new OtherModel(), checkerContext));
        assertTrue(allowAllOtherModelSecurityRulesDataAccessChecker.allowedToCreate(new OtherModel(), checkerContext));
    }

    @Test
    void should_not_allow_to_remove_when_roles_do_not_contain_necessary() {
        assertFalse(readAndCreateOnlyOtherModelSecurityRulesDataAccessChecker.allowedToRemove(new OtherModel(), checkerContext));
        assertTrue(readAndCreateOnlyOtherModelSecurityRulesDataAccessChecker.allowedToRemove(new OtherModel(), otherCheckerContext));
    }


    @Getter
    @Setter
    @NoArgsConstructor
    public static class TestModel implements Model {
        private TestModel testModel;
        private String owner;

        public TestModel(String owner, TestModel testModel) {
            this.owner = owner;
            this.testModel = testModel;
        }
    }

    private static class OtherModel implements Model {

    }
}