package edu.aau.se2.model;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import edu.aau.se2.model.listener.OnArmiesMovedListener;
import edu.aau.se2.model.listener.OnConnectionChangedListener;
import edu.aau.se2.model.listener.OnJoinedLobbyListener;
import edu.aau.se2.model.listener.OnPhaseChangedListener;
import edu.aau.se2.model.listener.OnPlayersChangedListener;
import edu.aau.se2.server.MainServer;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.data.Territory;
import edu.aau.se2.server.logic.ArmyCountHelper;


public class DatabaseSetupGameTest {
    private final int NUM_CLIENTS = 6;
    private final int TURNS_TO_PLAY = 16;
    private final int MOVE_EVERY_NTH_TURN = 4;

    private MainServer server;
    private ArrayList<Database> clients;
    private ArrayList<AtomicBoolean> clientsReceivedGameStarted;
    private ArrayList<Territory> unoccupiedTerritories;
    private ArrayList<ArrayList<Territory>> playerOccupiedTerritories;
    private AtomicInteger armiesPlaced;
    private AtomicInteger armyMovesCount;
    private AtomicInteger turnsPlayed;
    private AtomicInteger armiesReceivedInTurns;
    private AtomicBoolean setReady;

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
        turnsPlayed = new AtomicInteger(-1);
        armiesReceivedInTurns = new AtomicInteger(0);
        armyMovesCount = new AtomicInteger(0);
        setReady = new AtomicBoolean(false);

        for (int i=0; i<42; i++) {
            unoccupiedTerritories.add(new Territory(i));
        }
        for (int i=0; i<NUM_CLIENTS; i++) {
            playerOccupiedTerritories.add(new ArrayList<>());
        }
    }

    @Test
    public void testSetupGame() throws IOException, InterruptedException {
        startServer();
        Thread.sleep(1000);
        startClients();
        // wait for server and client to handle messages
        Thread.sleep(5000);

        for (AtomicBoolean b: clientsReceivedGameStarted) {
            Assert.assertTrue(b.get());
        }
        // check if all armies were placed (all clients count every ArmyPlacedMessage --> *NUM_CLIENTS*NUM_CLIENTS)
        Assert.assertEquals(ArmyCountHelper.getStartCount(NUM_CLIENTS)*NUM_CLIENTS*NUM_CLIENTS +        // initial armies
                armiesReceivedInTurns.get()*NUM_CLIENTS +                                                        // turn armies
                Math.ceil((float)TURNS_TO_PLAY/(float)MOVE_EVERY_NTH_TURN)*NUM_CLIENTS*2,                        // armies moved
                armiesPlaced.get(), 0);
        // check if all moves were successful
        Assert.assertEquals(Math.ceil((float)TURNS_TO_PLAY/(float)MOVE_EVERY_NTH_TURN)*NUM_CLIENTS,
                armyMovesCount.get(), 0);
    }

    private void startServer() throws IOException {
        server = new MainServer();
        server.start();
    }

    private void startClients() throws IOException {
        DatabaseTestSubclass.setServerAddress("localhost");
        for (int i=0; i<NUM_CLIENTS; i++) {
            Database db = new DatabaseTestSubclass();
            clients.add(db);
            int finalI = i;
            db.setGameStartListener((players, initialArmyCount) -> clientsReceivedGameStarted.get(finalI).set(true));
            db.setNextTurnListener((playerID, isThisPlayer) -> {
                Database curDB = clients.get(finalI);
                Assert.assertEquals(curDB.getCurrentPlayerToAct().getUid(), playerID);
                Assert.assertEquals(curDB.getThisPlayer().getUid() == playerID, isThisPlayer);

                if (isThisPlayer) {
                    curDB.armyPlaced(getNextTerritoryToPlaceArmy(finalI), 1);
                }
                if (db.isInitialArmyPlacementFinished() && db.isThisPlayersTurn()) {
                    turnsPlayed.addAndGet(1);
                }
            });
            db.setTerritoryUpdateListener((territoryID, armyCount, colorID) -> armiesPlaced.getAndAdd(1));
            db.setArmyReserveChangedListener((armyCount, isInitialCount) -> {
                Database curDB = clients.get(finalI);
                if (curDB.isInitialArmyPlacementFinished() && turnsPlayed.get() < TURNS_TO_PLAY) {
                    if (isInitialCount) {
                        armiesReceivedInTurns.addAndGet(armyCount);
                    }
                    curDB.armyPlaced(getNextTerritoryToPlaceArmy(finalI), 1);
                }
            });
            db.setArmiesMovedListener((playerID, fromTerritoryID, toTerritoryID, count) -> {
                armyMovesCount.addAndGet(1);
            });
            db.setPhaseChangedListener(newPhase -> {
                Database curDB = clients.get(finalI);

                if (newPhase == Database.Phase.ATTACKING && curDB.isThisPlayersTurn()) {
                    Assert.assertEquals(0, curDB.getCurrentArmyReserve());
                    curDB.finishAttackingPhase();
                }
                else if (newPhase == Database.Phase.MOVING && curDB.isThisPlayersTurn()) {
                    if (turnsPlayed.get() % MOVE_EVERY_NTH_TURN == 0) {
                        Territory fromTerritory = getRandomTerritoryOfPlayer(finalI);
                        curDB.armyMoved(fromTerritory.getId(),
                                        getRandomTerritoryOfPlayer(finalI).getId(),
                                        curDB.getTerritoryByID(fromTerritory.getId()).getArmyCount()-1);
                    }
                    else {
                        curDB.finishTurn();
                    }
                }
            });

            db.setConnectionChangedListener(new OnConnectionChangedListener() {
                @Override
                public void connected(Player thisPlayer) {
                    if (finalI == NUM_CLIENTS-1) {
                        db.hostLobby();
                    }
                }

                @Override
                public void disconnected() {

                }
            });
            db.setJoinedLobbyListener((lobbyID, host, players) -> {
                if (finalI == NUM_CLIENTS-1) {
                    joinLobby(lobbyID);
                }
            });
            db.setPlayersChangedListener(newPlayers -> {
                if (newPlayers.size() == NUM_CLIENTS && !setReady.get()) {
                    setReady.set(true);
                    for (Database c: clients) {
                        c.setPlayerReady(true);
                    }
                }
            });
            db.connectIfNotConnected();
        }
    }

    private synchronized void joinLobby(int lobbyID) {
        for (int i=0; i<NUM_CLIENTS-1; i++) {
            clients.get(i).joinLobby(lobbyID);
        }
    }

    private synchronized Territory getRandomTerritoryOfPlayer(int clientPlayerID) {
        Random rand = new Random();
        ArrayList<Territory> thisPlayersTerritories = playerOccupiedTerritories.get(clientPlayerID);
        return thisPlayersTerritories.get(rand.nextInt(thisPlayersTerritories.size()-1));
    }

    private synchronized int getNextTerritoryToPlaceArmy(int clientPlayerID) {
        if (!unoccupiedTerritories.isEmpty()) {
            Territory t = unoccupiedTerritories.remove(0);
            playerOccupiedTerritories.get(clientPlayerID).add(t);
            return t.getId();
        }
        else {
            return getRandomTerritoryOfPlayer(clientPlayerID).getId();
        }
    }

    @After
    public void teardown() {
        server.stop();
    }
}
