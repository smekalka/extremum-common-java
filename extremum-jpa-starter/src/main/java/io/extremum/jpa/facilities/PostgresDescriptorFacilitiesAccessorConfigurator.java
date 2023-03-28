package io.extremum.jpa.facilities;

import lombok.RequiredArgsConstructor;

import javax.annotation.PostConstruct;

@RequiredArgsConstructor
public class PostgresDescriptorFacilitiesAccessorConfigurator {
    private final PostgresDescriptorFacilities postgresDescriptorFacilities;

    @PostConstruct
    public void init() {
        StaticPostgresDescriptorFacilitiesAccessor.setFacilities(postgresDescriptorFacilities);
    }
}
