package edu.aau.se2.model;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import edu.aau.se2.model.listener.OnConnectionChangedListener;
import edu.aau.se2.server.MainServer;
import edu.aau.se2.server.data.Player;

public class HostLobbyTest extends AbstractDatabaseTest {
    private static final int NUM_CLIENTS = 2;

    private AtomicBoolean client1ReceivedJoinedLobbyMessage;
    private AtomicBoolean client2ReceivedJoinedLobbyMessage;
    private int client1LobbyID;
    private int client2LobbyID;

    public HostLobbyTest() {
        super(NUM_CLIENTS);
    }

    @Before
    public void setup() throws IOException {
        client1ReceivedJoinedLobbyMessage = new AtomicBoolean(false);
        client2ReceivedJoinedLobbyMessage = new AtomicBoolean(false);
        server.start();
    }

    @Test
    public void testHostLobby() throws IOException, InterruptedException {
        startClients();
        // wait for server and client to handle messages
        Thread.sleep(3000);

        Assert.assertTrue(client1ReceivedJoinedLobbyMessage.get());
        Assert.assertTrue(client2ReceivedJoinedLobbyMessage.get());
        Assert.assertNotEquals(client1LobbyID, client2LobbyID);
    }

    private void startClients() throws IOException {
        dbs[0].setJoinedLobbyListener((lobbyID, host, players) -> {
            client1ReceivedJoinedLobbyMessage.set(true);
            client1LobbyID = lobbyID;
        });
        dbs[0].setConnectionChangedListener(new OnConnectionChangedListener() {
            @Override
            public void connected(Player thisPlayer) {
                Assert.assertTrue(dbs[0].isConnected());
                dbs[0].hostLobby();
            }

            @Override
            public void disconnected() {

            }
        });

        dbs[1].setJoinedLobbyListener((lobbyID, host, players) -> {
            client2ReceivedJoinedLobbyMessage.set(true);
            client2LobbyID = lobbyID;
        });
        dbs[1].setConnectionChangedListener(new OnConnectionChangedListener() {
            @Override
            public void connected(Player thisPlayer) {
                Assert.assertTrue(dbs[1].isConnected());
                dbs[1].hostLobby();
            }

            @Override
            public void disconnected() {

            }
        });
        dbs[0].connectIfNotConnected();
        dbs[1].connectIfNotConnected();
        // test if connecting only when not connected
        dbs[1].connectIfNotConnected();
    }

    @After
    public void teardown() {
        disconnectAll();
    }
}
