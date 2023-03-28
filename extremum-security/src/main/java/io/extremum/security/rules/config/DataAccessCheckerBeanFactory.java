package io.extremum.security.rules.config;

import io.extremum.security.rules.provider.SecurityRuleProvider;
import io.extremum.security.rules.service.DataAccessCheckerFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.List;

@AllArgsConstructor
@Slf4j
public class DataAccessCheckerBeanFactory implements BeanFactoryPostProcessor {

    private final List<String> supportedModels;
    private final DataAccessCheckerFactory dataAccessCheckerFactory;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        for (String supportedModel : supportedModels) {
            String beanName = supportedModel + "-data-access-checker";
            log.info("Registering " + beanName);

            SecurityRuleProvider securityRuleProvider = beanFactory.getBean(SecurityRuleProvider.class);
            beanFactory.registerSingleton(beanName, dataAccessCheckerFactory.createSecurityRuleDataAccessChecker(supportedModel, securityRuleProvider));
        }
    }
}