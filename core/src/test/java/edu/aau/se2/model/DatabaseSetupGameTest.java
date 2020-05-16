package edu.aau.se2.model;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import edu.aau.se2.model.listener.OnConnectionChangedListener;
import edu.aau.se2.server.MainServer;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.data.Territory;
import edu.aau.se2.server.logic.ArmyCountHelper;
import edu.aau.se2.server.networking.MainServerTestable;


public class DatabaseSetupGameTest {
    /*private final int NUM_CLIENTS = 6;
    private final int TURNS_TO_PLAY = 16;
    private final int MOVE_EVERY_NTH_TURN = 4;

    private MainServerTestable server;
    private DatabaseTestable[] dbs;
    private ArrayList<AtomicBoolean> clientsReceivedGameStarted;
    private AtomicInteger armiesPlaced;
    private AtomicInteger armyMovesCount;
    private AtomicInteger turnsPlayed;
    private AtomicInteger armiesReceivedInTurns;
    private AtomicBoolean setReady;

    @Before
    public void setup() throws IOException {
        server = new MainServerTestable();
        server.start();
        setupClients();

        clientsReceivedGameStarted = new ArrayList<>();
        for (int i=0; i<NUM_CLIENTS; i++) {
            clientsReceivedGameStarted.add(new AtomicBoolean(false));
        }

        armiesPlaced = new AtomicInteger(0);
        turnsPlayed = new AtomicInteger(-1);
        armiesReceivedInTurns = new AtomicInteger(0);
        armyMovesCount = new AtomicInteger(0);
        setReady = new AtomicBoolean(false);
    }

    private void setupClients() {
        DatabaseTestable.setServerAddress("localhost");
        dbs = new DatabaseTestable[NUM_CLIENTS];
        for (int i=0; i<NUM_CLIENTS; i++) {
            dbs[i] = new DatabaseTestable();
        }
    }

    @Test
    public void testSetupGame() throws IOException, InterruptedException, TimeoutException {
        registerClientListeners();
        DatabaseTestable.setupLobby(dbs, 5000);
        DatabaseTestable.startLobby(dbs, 5000);
        DatabaseTestable.setupGame(dbs, 5000);
        registerClientListeners();
        DatabaseTestable clientToAct = DatabaseTestable.getClientToAct(dbs);
        clientToAct.armyPlaced(clientToAct.getNextTerritoryToPlaceArmiesOn().getId(), 1);

        // wait for server and client to handle messages
        Thread.sleep(5000);

        Assert.assertTrue(server.getDataStore().getLobbies().get(0).isStarted());

        int count = 0;
        for (AtomicBoolean b: clientsReceivedGameStarted) {
            if (b.get()) count++;
        }
        Assert.assertEquals(NUM_CLIENTS, count);

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

    private void registerClientListeners() {
        DatabaseTestable.setServerAddress("localhost");
        for (int i=0; i<NUM_CLIENTS; i++) {
            int finalI = i;
            dbs[i].setGameStartListener((players, initialArmyCount) -> clientsReceivedGameStarted.get(finalI).set(true));
            dbs[i].setNextTurnListener((playerID, isThisPlayer) -> {
                DatabaseTestable curDB = dbs[finalI];
                Assert.assertEquals(curDB.getCurrentPlayerToAct().getUid(), playerID);
                Assert.assertEquals(curDB.getThisPlayer().getUid() == playerID, isThisPlayer);

                if (isThisPlayer) {
                    curDB.armyPlaced(curDB.getNextTerritoryToPlaceArmiesOn().getId(), 1);
                }
                if (curDB.isInitialArmyPlacementFinished() && curDB.isThisPlayersTurn()) {
                    turnsPlayed.addAndGet(1);
                }
            });
            dbs[i].setTerritoryUpdateListener((territoryID, armyCount, colorID) -> armiesPlaced.getAndAdd(1));
            dbs[i].setArmyReserveChangedListener((armyCount, isInitialCount) -> {
                DatabaseTestable curDB = dbs[finalI];
                if (curDB.isInitialArmyPlacementFinished() && turnsPlayed.get() < TURNS_TO_PLAY) {
                    if (isInitialCount) {
                        armiesReceivedInTurns.addAndGet(armyCount);
                    }
                    curDB.armyPlaced(curDB.getNextTerritoryToPlaceArmiesOn().getId(), 1);
                }
            });
            dbs[i].setArmiesMovedListener((playerID, fromTerritoryID, toTerritoryID, count) -> {
                armyMovesCount.addAndGet(1);
            });
            dbs[i].setPhaseChangedListener(newPhase -> {
                DatabaseTestable curDB = dbs[finalI];

                if (newPhase == Database.Phase.ATTACKING && curDB.isThisPlayersTurn()) {
                    Assert.assertEquals(0, curDB.getCurrentArmyReserve());
                    curDB.finishAttackingPhase();
                }
                else if (newPhase == Database.Phase.MOVING && curDB.isThisPlayersTurn()) {
                    if (turnsPlayed.get() % MOVE_EVERY_NTH_TURN == 0) {
                        Territory fromTerritory = curDB.getMyTerritory(2);
                        curDB.armyMoved(fromTerritory.getId(),
                                curDB.getDifferentMyTerritory(fromTerritory.getId()).getId(),
                                        curDB.getTerritoryByID(fromTerritory.getId()).getArmyCount()-1);
                    }
                    else {
                        curDB.finishTurn();
                    }
                }
            });
        }
    }

    @After
    public void teardown() {
        server.stop();
    }*/
}
