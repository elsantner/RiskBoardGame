package edu.aau.se2.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import edu.aau.se2.model.listener.OnLeftGameListener;
import edu.aau.se2.model.listener.OnLeftLobbyListener;
import edu.aau.se2.model.listener.OnTerritoryUpdateListener;
import edu.aau.se2.model.listener.OnVictoryListener;
import edu.aau.se2.server.data.Lobby;
import edu.aau.se2.server.data.Territory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PlayerLeavesTest extends AbstractDatabaseTest {
    private static final int NUM_CLIENTS = 3;
    private AtomicInteger nextTurnMsgCount;
    private AtomicInteger victoryMsgCount;
    private AtomicInteger leftGameMsgCount;
    private AtomicInteger attackResultMsgCount;
    private AtomicInteger leftLobbyMsgCount;

    public PlayerLeavesTest() {
        super(NUM_CLIENTS);
    }

    @Before
    public void setup() throws IOException, TimeoutException {

        nextTurnMsgCount = new AtomicInteger(0);
        victoryMsgCount = new AtomicInteger(0);
        leftGameMsgCount = new AtomicInteger(0);
        attackResultMsgCount = new AtomicInteger(0);
        leftLobbyMsgCount = new AtomicInteger(0);
        server.start();
        setupScenario();
    }

    private void setupScenario() throws IOException, TimeoutException {
        DatabaseTestable.setupLobby(dbs, 5000);
        DatabaseTestable.startLobby(dbs, 5000);
        DatabaseTestable.setupGame(dbs, 5000);
        DatabaseTestable.placeTurnArmies(DatabaseTestable.getClientToAct(dbs), 5000);
    }

    /**
     * Test player leaves the game, its this players turn
     *
     * @throws InterruptedException
     */
    @Test
    public void testPlayerLeavesClientToAct() throws InterruptedException {
        DatabaseTestable clientToAct = DatabaseTestable.getClientToAct(dbs);

        for (DatabaseTestable db : dbs) {
            db.getListeners().setLeftGameListener((new OnLeftGameListener() {

                @Override
                public void removePlayerTerritories(List<Integer> ids) {

                    leftGameMsgCount.addAndGet(1);
                }
            }));
            db.getListeners().setNextTurnListener((playerID, isThisPlayer) -> nextTurnMsgCount.addAndGet(1));
            db.getListeners().setLeftLobbyListener(new OnLeftLobbyListener() {
                @Override
                public void leftLobby(boolean wasClosed) {
                    assertTrue(wasClosed);
                    leftLobbyMsgCount.addAndGet(1);
                }
            });
        }
        clientToAct.leaveLobby();

        Thread.sleep(2000);

        assertEquals(2, leftGameMsgCount.get());
        assertEquals(1, leftLobbyMsgCount.get());
        assertEquals(2, nextTurnMsgCount.get());
    }

    /**
     * PLayer leaves, not this players turn
     *
     * @throws InterruptedException
     */
    @Test
    public void testPlayerLeavesNotClientToAct() throws InterruptedException {
        DatabaseTestable clientToAct = DatabaseTestable.getClientToAct(dbs);
        DatabaseTestable otherClient = DatabaseTestable.getDifferentClient(dbs, clientToAct);

        for (DatabaseTestable db : dbs) {
            db.getListeners().setLeftGameListener(new OnLeftGameListener() {

                @Override
                public void removePlayerTerritories(List<Integer> ids) {

                    leftGameMsgCount.addAndGet(1);
                }
            });
            db.getListeners().setNextTurnListener((playerID, isThisPlayer) -> nextTurnMsgCount.addAndGet(1));
            db.getListeners().setLeftLobbyListener(new OnLeftLobbyListener() {
                @Override
                public void leftLobby(boolean wasClosed) {
                    assertTrue(wasClosed);
                    leftLobbyMsgCount.addAndGet(1);
                }
            });
        }
        otherClient.leaveLobby();

        Thread.sleep(2000);

        assertEquals(2, leftGameMsgCount.get());
        assertEquals(1, leftLobbyMsgCount.get());
        assertEquals(0, nextTurnMsgCount.get());
    }

    /**
     * Test that the leaving of a player that is involved in an attack is handled correctly
     *
     * @throws InterruptedException
     */
    @Test
    public void testPlayerLeavesDuringAttack() throws InterruptedException {
        DatabaseTestable clientToAct = DatabaseTestable.getClientToAct(dbs);
        DatabaseTestable otherClient = DatabaseTestable.getDifferentClient(dbs, clientToAct);
        Lobby l = clientToAct.getLobby();
        Lobby l1 = otherClient.getLobby();

        Territory attackerTerritory = l.getTerritoriesOccupiedByPlayer(clientToAct.getThisPlayer().getUid())[0];
        Territory defenderTerritory = l1.getTerritoriesOccupiedByPlayer(otherClient.getThisPlayer().getUid())[0];

        for (DatabaseTestable db : dbs) {
            db.getListeners().setLeftGameListener(new OnLeftGameListener() {
                @Override
                public void removePlayerTerritories(List<Integer> ids) {
                    leftGameMsgCount.addAndGet(1);
                }
            });
            db.getListeners().setNextTurnListener((playerID, isThisPlayer) -> nextTurnMsgCount.addAndGet(1));
            db.getListeners().setLeftLobbyListener(new OnLeftLobbyListener() {
                @Override
                public void leftLobby(boolean wasClosed) {
                    assertTrue(wasClosed);
                    leftLobbyMsgCount.addAndGet(1);
                }
            });
            db.getListeners().setTerritoryUpdateListener(new OnTerritoryUpdateListener() {
                @Override
                public void territoryUpdated(int territoryID, int armyCount, int colorID) {
                    attackResultMsgCount.addAndGet(1);
                }
            });
        }

        // if the armyCount is below 2 it is not possible to attack, get new territory
        for (int i = 1; attackerTerritory.getArmyCount() < 2; i++) {
            attackerTerritory = l.getTerritoriesOccupiedByPlayer(clientToAct.getThisPlayer().getUid())[i];
        }

        clientToAct.startAttack(attackerTerritory.getId(), defenderTerritory.getId(), 1);
        Thread.sleep(200);
        otherClient.leaveLobby();

        Thread.sleep(2000);

        assertEquals(2, leftGameMsgCount.get());
        assertEquals(1, leftLobbyMsgCount.get());
        assertEquals(0, nextTurnMsgCount.get());
        assertEquals(6, attackResultMsgCount.get());
    }

    /**
     * Test that the last player left wins, after everyone else left the game or disconnected
     *
     * @throws InterruptedException
     */
    @Test
    public void testAllLeaveLastOneWins() throws InterruptedException {
        DatabaseTestable clientToAct = DatabaseTestable.getClientToAct(dbs);
        DatabaseTestable otherClient = DatabaseTestable.getDifferentClient(dbs, clientToAct);

        for (DatabaseTestable db : dbs) {
            db.getListeners().setLeftGameListener(new OnLeftGameListener() {

                @Override
                public void removePlayerTerritories(List<Integer> ids) {
                    leftGameMsgCount.addAndGet(1);
                }
            });
            db.getListeners().setLeftLobbyListener(new OnLeftLobbyListener() {
                @Override
                public void leftLobby(boolean wasClosed) {
                    assertTrue(wasClosed);
                    leftLobbyMsgCount.addAndGet(1);
                }
            });
            db.getListeners().setVictoryListener(new OnVictoryListener() {
                @Override
                public void playerWon(String playerName, boolean thisPlayerWon) {
                    victoryMsgCount.addAndGet(1);
                }
            });
        }
        clientToAct.leaveLobby();
        otherClient.leaveLobby();

        Thread.sleep(2000);

        assertEquals(3, leftGameMsgCount.get());
        assertEquals(2, leftLobbyMsgCount.get());
        assertEquals(1, victoryMsgCount.get());
    }

    @After
    public void teardown() {
        disconnectAll();
    }
}
