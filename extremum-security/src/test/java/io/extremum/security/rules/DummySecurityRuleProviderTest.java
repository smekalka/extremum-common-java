package io.extremum.security.rules;

import io.extremum.security.rules.model.SecurityRule;
import io.extremum.security.rules.model.ServiceType;
import io.extremum.security.rules.provider.DummySecurityRuleProvider;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.List;

public class DummySecurityRuleProviderTest {

    @Test
    @SneakyThrows
    public void test() {
        DummySecurityRuleProvider dummySecurityRuleProvider = new DummySecurityRuleProvider();
        List<SecurityRule> rules = dummySecurityRuleProvider.getRules(ServiceType.DB);
//
//        SecurityRule securityRule = rules.get(0);
//        SecurityRule securityRule2 = rules.get(1);
//
//        Map<String, Object> scriptArgs = new HashMap<>();
//        scriptArgs.put("db", SecurityRule.DB());
//        scriptArgs.put("context", new Context(new User("Maxim", "email", Collections.singletonList("USER"))));
//        scriptArgs.put("object", new Object());

//        ScriptHost scriptHost = ScriptHost
//                .newBuilder()
//                .registry(JacksonRegistry.newRegistry())
//                .build();
//        Script script = scriptHost
//                .buildScript(securityRule.getExpression())
//                .withLibraries(new ExtremumSecurityLibrary())
//                .build();
//        Boolean execute = script.execute(Boolean.class, scriptArgs);
//        Val eval = securityRule.getProgram().eval(scriptArgs).getVal();

//        System.out.println(execute);

    }
}
