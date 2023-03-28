package io.extremum.jpa.facilities;

/**
 * @author rpuch
 */
public class StaticPostgresDescriptorFacilitiesAccessor {
    private static PostgresDescriptorFacilities facilitiesInstance;

    public static PostgresDescriptorFacilities getFacilities() {
        return facilitiesInstance;
    }

    public static void setFacilities(PostgresDescriptorFacilities facilities) {
        facilitiesInstance = facilities;
    }

    private StaticPostgresDescriptorFacilitiesAccessor() {}
}
