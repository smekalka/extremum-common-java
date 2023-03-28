package io.extremum.jpa;

import io.extremum.test.containers.CoreServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;

/**
 * @author rpuch
 */
class JpaServices extends CoreServices {
    private static final Logger LOGGER = LoggerFactory.getLogger(JpaServices.class);

    static {
        startPostgres();
    }

    private static void startPostgres() {
        GenericContainer postgres = new GenericContainer("postgres:11.3-alpine").withExposedPorts(5432);
        postgres.start();

        String postgresUrl = String.format("jdbc:postgresql://%s:%d/%s",
                postgres.getContainerIpAddress(), postgres.getFirstMappedPort(), "postgres");
        System.setProperty("jpa.uri", postgresUrl);
        LOGGER.info("Postgres DB url is {}", postgresUrl);
    }

}
