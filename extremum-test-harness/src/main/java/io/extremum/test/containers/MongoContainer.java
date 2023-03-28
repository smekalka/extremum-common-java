package io.extremum.test.containers;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

@Slf4j
public class MongoContainer implements AutoCloseable {
    private static final String DOCKER_IMAGE_NAME = "mongo:4.2.3-bionic";

    private final Network replicaSetNetwork;
    private final GenericContainer<?> container;

    public MongoContainer() {
        replicaSetNetwork = Network.newNetwork();

        container = new GenericContainer<>(DOCKER_IMAGE_NAME)
                .withNetwork(replicaSetNetwork)
                .withNetworkAliases("M1")
                .withCommand("--replSet rs0 --bind_ip localhost,M1")
                .withExposedPorts(27017);
        container.start();

        initReplicaSet(container);

        String mongoUri = "mongodb://" + container.getContainerIpAddress() + ":" + container.getFirstMappedPort();
        System.setProperty("mongo.uri", mongoUri);
        log.info("MongoDB uri is {}", mongoUri);
    }

    private static void initReplicaSet(GenericContainer<?> mongo) {
        try {
            mongo.execInContainer("/bin/bash", "-c",
                    "mongo --eval 'printjson(rs.initiate({_id:\"rs0\","
                            + "members:[{_id:0,host:\"M1:27017\"}]}))' "
                            + "--quiet");
            mongo.execInContainer("/bin/bash", "-c",
                    "until mongo --eval \"printjson(rs.isMaster())\" | grep ismaster | grep true > /dev/null 2>&1;"
                            + "do sleep 1;done");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initiate rs.", e);
        }
    }

    @Override
    public void close() {
        container.close();
        replicaSetNetwork.close();
    }
}
