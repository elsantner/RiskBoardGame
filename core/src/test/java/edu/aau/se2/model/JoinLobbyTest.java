package edu.aau.se2.model;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import edu.aau.se2.model.listener.OnConnectionChangedListener;
import edu.aau.se2.model.listener.OnJoinedLobbyListener;
import edu.aau.se2.server.MainServer;
import edu.aau.se2.server.data.Player;

public class JoinLobbyTest {
    private static final int NUM_CLIENTS = 3;

    private MainServer server;
    private Database[] clients;
    private AtomicInteger countJoinedMessages;

    @Before
    public void setup() {
        clients = new Database[NUM_CLIENTS];
        countJoinedMessages = new AtomicInteger(0);
    }

    @Test
    public void testJoinLobby() throws IOException, InterruptedException {
        startServer();
        Thread.sleep(1000);
        setupClients();
        clients[0].connectIfNotConnected();

        // wait for server and client to handle messages
        Thread.sleep(3000);

        Assert.assertEquals(NUM_CLIENTS, countJoinedMessages.get());
    }

    private void startServer() throws IOException {
        server = new MainServer();
        server.start();
    }

    private void setupClients() throws IOException {
        DatabaseTestSubclass.setServerAddress("localhost");

        for (int i=0; i<NUM_CLIENTS; i++) {
            clients[i] = new DatabaseTestSubclass();
        }
        clients[0].setConnectionChangedListener(new OnConnectionChangedListener() {
            @Override
            public void connected(Player thisPlayer) {
                clients[0].hostLobby();
            }

            @Override
            public void disconnected() {

            }
        });
        clients[0].setJoinedLobbyListener(new OnJoinedLobbyListener() {
            @Override
            public void joinedLobby(int lobbyID, Player host, List<Player> players) {
                countJoinedMessages.addAndGet(1);
                joinLobby();
            }
        });
    }

    private void joinLobby() {
        for (int i=1; i<NUM_CLIENTS; i++) {
            int finalI = i;
            clients[i].setConnectionChangedListener(new OnConnectionChangedListener() {
                @Override
                public void connected(Player thisPlayer) {
                    clients[finalI].joinLobby(clients[0].getCurrentLobbyID());
                }

                @Override
                public void disconnected() {

                }
            });
            clients[i].setJoinedLobbyListener(new OnJoinedLobbyListener() {
                @Override
                public void joinedLobby(int lobbyID, Player host, List<Player> players) {
                    Assert.assertEquals(clients[0].getThisPlayer().getUid(), host.getUid());
                    countJoinedMessages.addAndGet(1);
                }
            });

            try {
                clients[i].connectIfNotConnected();
            } catch (IOException e) {
                e.printStackTrace();    // handling not important because test will fail if this ex is thrown
            }
        }
    }

    @After
    public void teardown() {
        server.stop();
    }
}
