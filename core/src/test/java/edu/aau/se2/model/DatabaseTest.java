package edu.aau.se2.model;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import edu.aau.se2.model.listener.OnGameStartListener;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.networking.SerializationRegister;
import edu.aau.se2.server.networking.dto.StartGameMessage;
import edu.aau.se2.server.networking.kryonet.NetworkServerKryo;


public class DatabaseTest {
    private AtomicBoolean clientGameStartReceived;
    private NetworkServerKryo server;
    private ArrayList<Player> playerList;

    @Before
    public void setup() {
        clientGameStartReceived = new AtomicBoolean(false);
    }

    @Test
    public void NetworkConnection_OneClient_SendAndReceiveText() throws IOException, InterruptedException {
        startServer();
        startClient();
        // wait for server and client to handle messages
        Thread.sleep(1500);

        playerList = new ArrayList<>(Arrays.asList(new Player(0, "P1"), new Player(1, "P2")));
        StartGameMessage startGameMessage = new StartGameMessage(0, 0, playerList, 35);
        server.broadcastMessage(startGameMessage);

        Thread.sleep(1500);

        Assert.assertTrue(clientGameStartReceived.get());
    }

    private void startServer() throws IOException {
        server = new NetworkServerKryo();
        SerializationRegister.registerClassesForComponent(server);
        server.start();
    }

    private void startClient() {
        Database.setServerAddress("localhost");
        Database db = Database.getInstance();
        db.setOnGameStartListener(new OnGameStartListener() {
            @Override
            public void onGameStarted(ArrayList<Player> players, int initialArmyCount) {
                Assert.assertEquals(playerList.size(), players.size());
                Assert.assertEquals(35, initialArmyCount);
                Assert.assertEquals(35, db.getCurrentArmyReserve());
                clientGameStartReceived.set(true);
            }
        });
    }

    @After
    public void teardown() {
        server.stop();
    }
}
