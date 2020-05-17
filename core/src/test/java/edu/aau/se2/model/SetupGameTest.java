package edu.aau.se2.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import edu.aau.se2.server.logic.ArmyCountHelper;

import static org.junit.Assert.*;

public class SetupGameTest extends AbstractDatabaseTest {
    private static final int NUM_CLIENTS = 3;

    private AtomicInteger territoryUpdateCount;

    public SetupGameTest() {
        super(NUM_CLIENTS);
    }

    @Before
    public void setup() throws IOException, TimeoutException {
        territoryUpdateCount = new AtomicInteger(0);
        server.start();
        setupScenario();
    }

    private void setupScenario() throws IOException, TimeoutException {
        DatabaseTestable.setupLobby(dbs, 5000);
        DatabaseTestable.startLobby(dbs, 5000);
    }

    /**
     * Test placing of initial armies.
     * @throws InterruptedException
     */
    @Test
    public void testSetupGame() throws InterruptedException {
        for (DatabaseTestable db : dbs) {
            db.getListeners().setTerritoryUpdateListener((territoryID, armyCount, colorID) -> territoryUpdateCount.addAndGet(1));
            db.getListeners().setNextTurnListener((playerID, isThisPlayer) -> {
                if (isThisPlayer && !db.getLobby().areInitialArmiesPlaced()) {
                    assertEquals(Database.Phase.NONE, db.getCurrentPhase());
                    db.armyPlaced(db.getNextTerritoryToPlaceArmiesOn().getId(), 1);
                }
            });
        }
        DatabaseTestable clientToAct = DatabaseTestable.getClientToAct(dbs);
        clientToAct.armyPlaced(clientToAct.getNextTerritoryToPlaceArmiesOn().getId(), 1);

        Thread.sleep(2000);
        assertEquals(ArmyCountHelper.getStartCount(NUM_CLIENTS)*NUM_CLIENTS*NUM_CLIENTS, territoryUpdateCount.get());
        int armyCountGreaterZero = 0;
        for (DatabaseTestable db : dbs) {
            armyCountGreaterZero += db.getCurrentArmyReserve() > 0 ? 1 : 0;
            assertTrue(db.getLobby().areInitialArmiesPlaced());
            assertEquals(Database.Phase.PLACING, db.getCurrentPhase());
        }
        assertEquals(1, armyCountGreaterZero);
    }

    @After
    public void teardown() {
        disconnectAll();
    }
}
