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

public class HostLobbyTest {
    private MainServer server;

    private AtomicBoolean client1ReceivedJoinedLobbyMessage;
    private AtomicBoolean client2ReceivedJoinedLobbyMessage;
    int client1LobbyID;
    int client2LobbyID;

    @Before
    public void setup() {
        client1ReceivedJoinedLobbyMessage = new AtomicBoolean(false);
        client2ReceivedJoinedLobbyMessage = new AtomicBoolean(false);
    }

    @Test
    public void testHostLobby() throws IOException, InterruptedException {
        startServer();
        Thread.sleep(1000);
        startClients();
        // wait for server and client to handle messages
        Thread.sleep(3000);

        Assert.assertTrue(client1ReceivedJoinedLobbyMessage.get());
        Assert.assertTrue(client2ReceivedJoinedLobbyMessage.get());
        Assert.assertNotEquals(client1LobbyID, client2LobbyID);
    }

    private void startServer() throws IOException {
        server = new MainServer(true);
        server.start();
    }

    private void startClients() throws IOException {
        DatabaseTestSubclass.setServerAddress("localhost");
        Database db1 = new DatabaseTestSubclass();
        Database db2 = new DatabaseTestSubclass();

        db1.setJoinedLobbyListener((lobbyID, host, players) -> {
            client1ReceivedJoinedLobbyMessage.set(true);
            client1LobbyID = lobbyID;
        });
        db1.setConnectionChangedListener(new OnConnectionChangedListener() {
            @Override
            public void connected(Player thisPlayer) {
                Assert.assertTrue(db1.isConnected());
                db1.hostLobby();
            }

            @Override
            public void disconnected() {

            }
        });

        db2.setJoinedLobbyListener((lobbyID, host, players) -> {
            client2ReceivedJoinedLobbyMessage.set(true);
            client2LobbyID = lobbyID;
        });
        db2.setConnectionChangedListener(new OnConnectionChangedListener() {
            @Override
            public void connected(Player thisPlayer) {
                Assert.assertTrue(db2.isConnected());
                db2.hostLobby();
            }

            @Override
            public void disconnected() {

            }
        });
    }

    @After
    public void teardown() {
        server.stop();
    }
}
