package io.extremum.watch.config;

import io.extremum.everything.services.management.ModelRetriever;
import io.extremum.security.DataSecurity;
import io.extremum.security.PrincipalSource;
import io.extremum.watch.services.DefaultModelSignalProvider;
import io.extremum.watch.services.ModelSignalProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

@Configuration
@IntegrationComponentScan("io.extremum.watch.aop")
public class CaptureChangesConfig {

    @Bean
    public SubscribableChannel modelSignalsMessageChannel() {
        return new PublishSubscribeChannel(securityContextExecutor(executor()));
    }

    @Bean
    public ThreadPoolTaskExecutor executor() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(10);
        pool.setMaxPoolSize(10);
        pool.setWaitForTasksToCompleteOnShutdown(true);

        return pool;
    }

    private DelegatingSecurityContextAsyncTaskExecutor securityContextExecutor(ThreadPoolTaskExecutor delegate) {
        return new DelegatingSecurityContextAsyncTaskExecutor(delegate);
    }

    @Bean
    @ConditionalOnMissingBean
    public ModelSignalProvider modelSignalProvider(DataSecurity dataSecurity, ModelRetriever modelRetriever, PrincipalSource principalSource) {
        return new DefaultModelSignalProvider(dataSecurity, modelRetriever, principalSource);
    }
}