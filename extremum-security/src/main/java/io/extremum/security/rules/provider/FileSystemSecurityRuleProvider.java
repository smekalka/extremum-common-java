package io.extremum.security.rules.provider;

import io.extremum.security.rules.model.SecurityRule;
import io.extremum.security.rules.model.ServiceType;
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class FileSystemSecurityRuleProvider extends AbstractSecurityRuleProvider {

    private final String securityRulesTextFileName;

    public FileSystemSecurityRuleProvider(String securityRulesTextFileName) {
        this.securityRulesTextFileName = securityRulesTextFileName;
    }

    private static final String RULES_DELIMITER = ";";

    @Override
    @SneakyThrows
    protected List<String> getRulesStringsList() {
        return Arrays.asList(readFile(securityRulesTextFileName, Charset.defaultCharset()).split(RULES_DELIMITER));
    }

    @Override
    public List<SecurityRule> getRules(ServiceType serviceType) {
        serviceTypeToSecurityRulesMap = composeServiceTypeToSecurityRulesMap(getRulesStringsList());
        return Optional.ofNullable(serviceTypeToSecurityRulesMap.get(serviceType)).orElse(Collections.emptyList());
    }

    static String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}
