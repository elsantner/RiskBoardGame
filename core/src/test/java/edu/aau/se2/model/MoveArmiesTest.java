package edu.aau.se2.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import edu.aau.se2.server.data.Territory;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class MoveArmiesTest extends AbstractDatabaseTest {
    private static final int NUM_CLIENTS = 3;

    private AtomicInteger territoryUpdateCount;
    private AtomicInteger armiesMovedCount;
    private AtomicInteger phaseChangedCount;
    private AtomicInteger nextTurnCount;

    public MoveArmiesTest() {
        super(NUM_CLIENTS);
    }

    @Before
    public void setup() throws IOException, TimeoutException {
        territoryUpdateCount = new AtomicInteger(0);
        phaseChangedCount = new AtomicInteger(0);
        armiesMovedCount = new AtomicInteger(0);
        nextTurnCount = new AtomicInteger(0);
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
     * Test placing armies during placing phase of a turn.
     * @throws InterruptedException
     */
    @Test
    public void testMoveArmies() throws InterruptedException {
        DatabaseTestable clientToAct = DatabaseTestable.getClientToAct(dbs);
        Territory fromTerritory = clientToAct.getMyTerritory(2);
        Territory toTerritory = clientToAct.getDifferentMyTerritory(fromTerritory.getId());
        int armiesCount = fromTerritory.getArmyCount()-1;

        for (DatabaseTestable db : dbs) {
            db.getListeners().setArmiesMovedListener((playerID, fromTerritoryID, toTerritoryID, count) -> {
                assertEquals(fromTerritory.getId(), fromTerritoryID);
                assertEquals(toTerritory.getId(), toTerritoryID);
                assertEquals(armiesCount, count);
                armiesMovedCount.addAndGet(1);
            });
            db.getListeners().setTerritoryUpdateListener((territoryID, armyCount, colorID) -> territoryUpdateCount.addAndGet(1));
            db.getListeners().setPhaseChangedListener(newPhase -> {
                assertThat(newPhase, anyOf(is(Database.Phase.MOVING), is(Database.Phase.PLACING)));
                phaseChangedCount.addAndGet(1);
                if (newPhase == Database.Phase.MOVING && db.isThisPlayersTurn()) {
                    db.armyMoved(fromTerritory.getId(), toTerritory.getId(), armiesCount);
                }
            });
            db.getListeners().setNextTurnListener((playerID, isThisPlayer) -> nextTurnCount.addAndGet(1));
        }

        clientToAct.finishAttackingPhase();

        Thread.sleep(2000);
        assertEquals(NUM_CLIENTS*2, territoryUpdateCount.get());
        assertEquals(NUM_CLIENTS, armiesMovedCount.get());
        assertEquals(NUM_CLIENTS*2, phaseChangedCount.get());
        assertEquals(NUM_CLIENTS, nextTurnCount.get());
        for (DatabaseTestable db : dbs) {
            assertEquals(Database.Phase.PLACING, db.getCurrentPhase());
            assertEquals(1, db.getTerritoryByID(fromTerritory.getId()).getArmyCount());
            assertEquals(clientToAct.getThisPlayer().getUid(), db.getTerritoryByID(fromTerritory.getId()).getOccupierPlayerID());
        }
    }

    @After
    public void teardown() {
        disconnectAll();
    }
}
