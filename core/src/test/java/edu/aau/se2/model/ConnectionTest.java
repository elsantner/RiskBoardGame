package edu.aau.se2.model;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import edu.aau.se2.server.networking.NetworkClient;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConnectionTest extends AbstractDatabaseTest {
    private static final int NUM_CLIENTS = 1;

    private AtomicBoolean connectCalled;
    private AtomicBoolean disconnectCalled;

    public ConnectionTest() {
        super(NUM_CLIENTS);
    }

    @Before
    public void setup() throws IOException {
        connectCalled = new AtomicBoolean(false);
        disconnectCalled = new AtomicBoolean(false);
        server.start();
    }

    @Test
    public void testConnect() throws IOException, InterruptedException {
        startClients();
        // wait for server and client to handle messages
        Thread.sleep(3000);

        Assert.assertTrue(connectCalled.get());
        Assert.assertTrue(disconnectCalled.get());
    }

    private void startClients() throws IOException {
        dbs[0].getClient().registerConnectionListener(new NetworkClient.OnConnectionChangedListener() {
            @Override
            public void connected() {
                connectCalled.set(true);
            }

            @Override
            public void disconnected() {
                disconnectCalled.set(true);
            }
        });

        assertFalse(dbs[0].getClient().isConnected());
        assertTrue(dbs[0].connectIfNotConnected());
        assertTrue(dbs[0].getClient().isConnected());
        // test if connecting only when not connected
        assertFalse(dbs[0].connectIfNotConnected());
        assertTrue(dbs[0].getClient().isConnected());

        dbs[0].closeConnection();
    }

    @After
    public void teardown() {
        disconnectAll();
    }
}
