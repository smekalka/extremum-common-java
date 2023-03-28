package io.extremum.security.rules.provider;

import java.util.Arrays;
import java.util.List;

public class DummySecurityRuleProvider extends AbstractSecurityRuleProvider {

    public static final String RULE_1 = "db" +
            ".path('/goods/*')" +
            ".when('Maxim' == context.user.name)" +
            ".allow(['read', 'update', 'delete'])";
    public static final String RULE_2 = "db" +
            ".path('/orders/*')" +
            ".allow(['read'])";

    @Override
    protected List<String> getRulesStringsList() {
        return Arrays.asList(RULE_1, RULE_2);
    }
}
