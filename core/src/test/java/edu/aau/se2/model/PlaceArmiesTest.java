package edu.aau.se2.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class PlaceArmiesTest extends AbstractDatabaseTest {
    private static final int NUM_CLIENTS = 3;

    private AtomicInteger territoryUpdateCount;
    private AtomicInteger phaseChangedCount;

    public PlaceArmiesTest() {
        super(NUM_CLIENTS);
    }

    @Before
    public void setup() throws IOException, TimeoutException {
        territoryUpdateCount = new AtomicInteger(0);
        phaseChangedCount = new AtomicInteger(0);
        server.start();
        setupScenario();
    }

    private void setupScenario() throws IOException, TimeoutException {
        DatabaseTestable.setupLobby(dbs, 5000);
        DatabaseTestable.startLobby(dbs, 5000);
        DatabaseTestable.setupGame(dbs, 5000);
    }

    /**
     * Test placing armies during placing phase of a turn.
     * @throws InterruptedException
     */
    @Test
    public void testPlaceArmies() throws InterruptedException {
        for (DatabaseTestable db : dbs) {
            db.setTerritoryUpdateListener((territoryID, armyCount, colorID) -> territoryUpdateCount.addAndGet(1));
            db.setPhaseChangedListener(newPhase -> {
                assertEquals(Database.Phase.ATTACKING, newPhase);
                phaseChangedCount.addAndGet(1);
            });
        }

        DatabaseTestable clientToAct = DatabaseTestable.getClientToAct(dbs);
        int armiesToPlace = clientToAct.getCurrentArmyReserve();

        clientToAct.setArmyReserveChangedListener((armyCount, isInitialCount) -> {
            if (armyCount > 0) {
                clientToAct.armyPlaced(clientToAct.getNextTerritoryToPlaceArmiesOn().getId(), 1);
            }
        });
        clientToAct.armyPlaced(clientToAct.getNextTerritoryToPlaceArmiesOn().getId(), 1);

        Thread.sleep(2000);
        assertEquals(NUM_CLIENTS*armiesToPlace, territoryUpdateCount.get());
        assertEquals(NUM_CLIENTS, phaseChangedCount.get());
        for (DatabaseTestable db : dbs) {
            assertEquals(0, db.getCurrentArmyReserve());
            assertEquals(Database.Phase.ATTACKING, db.getCurrentPhase());
        }
    }

    @After
    public void teardown() {
        disconnectAll();
    }
}
