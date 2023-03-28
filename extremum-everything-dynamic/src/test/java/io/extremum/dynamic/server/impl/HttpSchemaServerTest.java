package io.extremum.dynamic.server.impl;

import io.extremum.dynamic.server.handlers.HttpSchemaServerHandler;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.util.SocketUtils;

import java.io.IOException;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpSchemaServerTest {
    @Test
    void launch() {
    }

    @Test
    void shutdownWillReleasePort() throws InterruptedException {
        int port = SocketUtils.findAvailableTcpPort();

        HttpSchemaServerHandler handler = Mockito.mock(HttpSchemaServerHandler.class);

        HttpSchemaServer server = new HttpSchemaServer(port, "/", handler);

        assertFalse(isPortInUse(port));
        assertFalse(server.isRunning());

        server.launch();

        Thread.sleep(2000);

        assertTrue(isPortInUse(port));
        assertTrue(server.isRunning());

        server.shutdown();

        Thread.sleep(2000);

        assertFalse(isPortInUse(port));
        assertFalse(server.isRunning());
    }

    private boolean isPortInUse(int port) {
        try {
            new Socket("localhost", port).close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}