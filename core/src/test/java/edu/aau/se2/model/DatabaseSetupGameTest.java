package edu.aau.se2.model;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import edu.aau.se2.model.listener.OnGameStartListener;
import edu.aau.se2.model.listener.OnNextTurnListener;
import edu.aau.se2.model.listener.OnTerritoryUpdateListener;
import edu.aau.se2.server.MainServer;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.data.Territory;
import edu.aau.se2.server.logic.ArmyCountHelper;


public class DatabaseSetupGameTest {
    private final int NUM_CLIENTS = 2;

    private MainServer server;
    private ArrayList<Database> clients;
    private ArrayList<AtomicBoolean> clientsReceivedGameStarted;
    private ArrayList<Territory> unoccupiedTerritories;
    private ArrayList<ArrayList<Territory>> playerOccupiedTerritories;
    private AtomicInteger armiesPlaced;

    @Before
    public void setup() {
        clients = new ArrayList<>();
        clientsReceivedGameStarted = new ArrayList<>();
        for (int i=0; i<NUM_CLIENTS; i++) {
            clientsReceivedGameStarted.add(new AtomicBoolean(false));
        }

        unoccupiedTerritories = new ArrayList<>();
        playerOccupiedTerritories = new ArrayList<>();
        armiesPlaced = new AtomicInteger(0);
        for (int i=0; i<42; i++) {
            unoccupiedTerritories.add(new Territory(i+1));
        }
        for (int i=0; i<NUM_CLIENTS; i++) {
            playerOccupiedTerritories.add(new ArrayList<>());
        }
    }

    @Test
    public void testSetupGame() throws IOException, InterruptedException {
        startServer();
        startClients();
        // wait for server and client to handle messages
        Thread.sleep(5000);

        for (AtomicBoolean b: clientsReceivedGameStarted) {
            Assert.assertTrue(b.get());
        }
        // check if all armies were placed (all clients count every ArmyPlacedMessage --> *NUM_CLIENTS*NUM_CLIENTS)
        Assert.assertEquals(ArmyCountHelper.getStartCount(NUM_CLIENTS)*NUM_CLIENTS*NUM_CLIENTS, armiesPlaced.get());
    }

    private void startServer() throws IOException {
        server = new MainServer();
        server.start();
    }

    private void startClients() {
        DatabaseTestSubclass.setServerAddress("localhost");
        for (int i=0; i<NUM_CLIENTS; i++) {
            Database db = new DatabaseTestSubclass();
            clients.add(db);
            int finalI = i;
            db.setOnGameStartListener(new OnGameStartListener() {
                @Override
                public void onGameStarted(ArrayList<Player> players, int initialArmyCount) {
                    clientsReceivedGameStarted.get(finalI).set(true);
                }
            });
            db.setOnNextTurnListener(new OnNextTurnListener() {
                @Override
                public void isPlayersTurnNow(int playerID, boolean isThisPlayer) {
                    Database curDB = clients.get(finalI);
                    Assert.assertEquals(curDB.getCurrentPlayerToAct().getUid(), playerID);
                    Assert.assertEquals(curDB.getThisPlayer().getUid() == playerID, isThisPlayer);
                    if (isThisPlayer) {
                        curDB.armyPlaced(getNextTerritoryToPlaceArmy(finalI), 1);
                    }
                }
            });
            db.setOnTerritoryUpdateListener(new OnTerritoryUpdateListener() {
                @Override
                public void territoryUpdated(int territoryID, int armyCount, int colorID) {
                    armiesPlaced.getAndAdd(1);
                }
            });
        }
        for (Database db: clients) {
            db.setPlayerReady(true);
        }
    }

    private synchronized int getNextTerritoryToPlaceArmy(int clientPlayerID) {
        if (!unoccupiedTerritories.isEmpty()) {
            Territory t = unoccupiedTerritories.remove(0);
            playerOccupiedTerritories.get(clientPlayerID).add(t);
            return t.getId();
        }
        else {
            Random rand = new Random();
            ArrayList<Territory> thisPlayersTerritories = playerOccupiedTerritories.get(clientPlayerID);
            return thisPlayersTerritories.get(rand.nextInt(thisPlayersTerritories.size()-1)).getId();
        }
    }

    @After
    public void teardown() {
        server.stop();
    }
}
