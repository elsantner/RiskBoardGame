package edu.aau.se2.model;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import edu.aau.se2.model.listener.OnConnectionChangedListener;
import edu.aau.se2.server.MainServer;
import edu.aau.se2.server.data.Player;

public class JoinLobbyTest extends AbstractDatabaseTest {
    private static final int NUM_CLIENTS = 3;

    private AtomicInteger countJoinedMessages;
    private AtomicInteger countLeftMessages;
    private AtomicInteger errorCount;

    public JoinLobbyTest() {
        super(NUM_CLIENTS);
    }

    @Before
    public void setup() throws IOException {
        countJoinedMessages = new AtomicInteger(0);
        countLeftMessages = new AtomicInteger(0);
        errorCount = new AtomicInteger(0);
        server.start();
    }

    @Test
    public void testJoinLobby() throws IOException, InterruptedException {
        setupClients();
        dbs[0].connectIfNotConnected();

        // wait for server and client to handle messages
        Thread.sleep(3000);

        Assert.assertEquals(NUM_CLIENTS, countJoinedMessages.get());
        Assert.assertEquals(NUM_CLIENTS-1, countLeftMessages.get());
        Assert.assertEquals(2, errorCount.get());
        Assert.assertEquals(1, dbs[0].getCurrentPlayers().size());      // since all but the host have left again
    }

    private void setupClients() {

        for (int i=0; i<NUM_CLIENTS; i++) {
            dbs[i].getListeners().setErrorListener(errorCode -> errorCount.addAndGet(1));
        }
        dbs[0].getListeners().setConnectionChangedListener(new OnConnectionChangedListener() {
            @Override
            public void connected(Player thisPlayer) {
                dbs[0].hostLobby();
            }

            @Override
            public void disconnected() {

            }
        });
        dbs[0].getListeners().setJoinedLobbyListener((lobbyID, host, players) -> {
            countJoinedMessages.addAndGet(1);
            // trigger first error: joining already joined lobby
            dbs[0].joinLobby(dbs[0].getCurrentLobbyID());
            letClientsJoinLobby();
        });
    }

    private void letClientsJoinLobby() {
        for (int i=1; i<NUM_CLIENTS; i++) {
            int finalI = i;
            dbs[i].getListeners().setConnectionChangedListener(new OnConnectionChangedListener() {
                @Override
                public void connected(Player thisPlayer) {
                    // trigger second error: joining non-existent lobby
                    if (finalI == 1) {
                        dbs[1].joinLobby(999);
                    }

                    dbs[finalI].triggerLobbyListUpdate();
                }

                @Override
                public void disconnected() {

                }
            });
            dbs[i].getListeners().setLobbyListChangedListener(lobbyList -> dbs[finalI].joinLobby(lobbyList.get(0).getLobbyID()));

            dbs[i].getListeners().setJoinedLobbyListener((lobbyID, host, players) -> {
                Assert.assertEquals(dbs[0].getThisPlayer().getUid(), host.getUid());
                countJoinedMessages.addAndGet(1);
                dbs[finalI].leaveLobby();       // leaf lobby again
            });
            dbs[i].getListeners().setLeftLobbyListener((wasClosed) -> {
                countLeftMessages.addAndGet(1);
            });

            try {
                dbs[i].connectIfNotConnected();
            } catch (IOException e) {
                e.printStackTrace();    // handling not important because test will fail if this ex is thrown
            }
        }
    }

    @After
    public void teardown() {
        disconnectAll();
    }
}
