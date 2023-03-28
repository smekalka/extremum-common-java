package io.extremum.sku.service;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.scheduling.annotation.Scheduled;

@AllArgsConstructor
public class ScheduledDataBaseMonitor {
    private final DataBaseMetricsFetcher dataBaseMetricsFetcher;

    @SneakyThrows
    @Scheduled(fixedDelayString = "${extremum.sku.entities.volume.delay:5000}")
    public void getDataBaseSize() {
        dataBaseMetricsFetcher.getDataBaseSize();
    }
}
