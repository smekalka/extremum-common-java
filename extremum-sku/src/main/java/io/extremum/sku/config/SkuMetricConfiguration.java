package io.extremum.sku.config;

import io.extremum.sku.aop.SkuMetricAspect;
import io.extremum.sku.service.DataBaseMetricsFetcher;
import io.extremum.sku.service.PostgresDataBaseMetricsFetcher;
import io.extremum.sku.service.ScheduledDataBaseMonitor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.persistence.EntityManager;

@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "extremum", value = "sku.enabled", havingValue = "true")
public class SkuMetricConfiguration {
    @Bean
    public SkuMetricAspect scuMetricAspect(ApplicationContext applicationContext) {
        return new SkuMetricAspect(applicationContext);
    }

    @SneakyThrows
    @Bean
    public ScheduledDataBaseMonitor dataBaseSizeMonitor(DataBaseMetricsFetcher dataBaseMetricsFetcher) {
        return new ScheduledDataBaseMonitor(dataBaseMetricsFetcher);
    }

    @SneakyThrows
    @Bean
    @ConditionalOnProperty(prefix = "spring", name = "jpa.database", havingValue = "postgresql")
    public DataBaseMetricsFetcher dataBaseMetricsFetcher(EntityManager entityManager, @Value("${spring.datasource.url}") String dataSourceUrl) {
        String dbName = StringUtils.substringAfterLast(dataSourceUrl, '/');
        dbName = StringUtils.substringBefore(dbName, '?');

        return new PostgresDataBaseMetricsFetcher(dbName, entityManager);
    }

    @Bean
    public QueueChannel skuMetricMessageChannel() {
        return new QueueChannel(1000_00);
    }
}
