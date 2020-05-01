package edu.aau.se2.model;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import edu.aau.se2.model.listener.OnConnectionChangedListener;
import edu.aau.se2.model.listener.OnJoinedLobbyListener;
import edu.aau.se2.model.listener.OnLeftLobbyListener;
import edu.aau.se2.model.listener.OnLobbyListChangedListener;
import edu.aau.se2.server.MainServer;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.networking.dto.prelobby.LobbyListMessage;

public class JoinLobbyTest {
    private static final int NUM_CLIENTS = 3;

    private MainServer server;
    private Database[] clients;
    private AtomicInteger countJoinedMessages;
    private AtomicInteger countLeftMessages;
    private AtomicInteger errorCount;

    @Before
    public void setup() {
        clients = new Database[NUM_CLIENTS];
        countJoinedMessages = new AtomicInteger(0);
        countLeftMessages = new AtomicInteger(0);
        errorCount = new AtomicInteger(0);
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
        Assert.assertEquals(NUM_CLIENTS-1, countLeftMessages.get());
        Assert.assertEquals(2, errorCount.get());
        Assert.assertEquals(1, clients[0].getCurrentPlayers().size());      // since all but the host have left again
    }

    private void startServer() throws IOException {
        server = new MainServer();
        server.start();
    }

    private void setupClients() throws IOException {
        DatabaseTestSubclass.setServerAddress("localhost");

        for (int i=0; i<NUM_CLIENTS; i++) {
            clients[i] = new DatabaseTestSubclass();
            clients[i].setErrorListener(errorCode -> errorCount.addAndGet(1));
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
        clients[0].setJoinedLobbyListener((lobbyID, host, players) -> {
            countJoinedMessages.addAndGet(1);
            // trigger first error: joining already joined lobby
            clients[0].joinLobby(clients[0].getCurrentLobbyID());
            letClientsJoinLobby();
        });
    }

    private void letClientsJoinLobby() {
        for (int i=1; i<NUM_CLIENTS; i++) {
            int finalI = i;
            clients[i].setConnectionChangedListener(new OnConnectionChangedListener() {
                @Override
                public void connected(Player thisPlayer) {
                    // trigger second error: joining non-existent lobby
                    if (finalI == 1) {
                        clients[1].joinLobby(999);
                    }

                    clients[finalI].triggerLobbyListUpdate();
                }

                @Override
                public void disconnected() {

                }
            });
            clients[i].setLobbyListChangedListener(lobbyList -> clients[finalI].joinLobby(lobbyList.get(0).getLobbyID()));

            clients[i].setJoinedLobbyListener((lobbyID, host, players) -> {
                Assert.assertEquals(clients[0].getThisPlayer().getUid(), host.getUid());
                countJoinedMessages.addAndGet(1);
                clients[finalI].leaveLobby();       // leaf lobby again
            });
            clients[i].setLeftLobbyListener(() -> {
                countLeftMessages.addAndGet(1);
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
