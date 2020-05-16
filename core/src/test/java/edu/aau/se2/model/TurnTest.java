package edu.aau.se2.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TurnTest extends AbstractDatabaseTest {
    private static final int NUM_CLIENTS = 3;
    private static final int NUM_TURNS = 5;

    private AtomicInteger phaseChangedCount;
    private AtomicInteger nextTurnCount;
    private AtomicInteger turnsPlayed;

    public TurnTest() {
        super(NUM_CLIENTS);
    }

    @Before
    public void setup() throws IOException, TimeoutException {
        phaseChangedCount = new AtomicInteger(0);
        nextTurnCount = new AtomicInteger(0);
        turnsPlayed = new AtomicInteger(0);
        server.start();
        setupScenario();
    }

    private void setupScenario() throws IOException, TimeoutException {
        DatabaseTestable.setupLobby(dbs, 5000);
        DatabaseTestable.startLobby(dbs, 5000);
        DatabaseTestable.setupGame(dbs, 5000);
    }

    /**
     * Test playing multiple "mock turns" (no attacking or moving or other unnecessary steps).
     * @throws InterruptedException
     */
    @Test
    public void testPlayTurns() throws InterruptedException, TimeoutException {
        for (DatabaseTestable db : dbs) {
            db.setArmyReserveChangedListener((armyCount, isInitialCount) -> {
                if (armyCount > 0) {
                    db.armyPlaced(db.getNextTerritoryToPlaceArmiesOn().getId(), 1);
                }
            });
            db.setPhaseChangedListener(newPhase -> {
                try {
                    phaseChangedCount.addAndGet(1);
                    if (turnsPlayed.get() < NUM_TURNS) {
                        if (newPhase == Database.Phase.ATTACKING && db.isThisPlayersTurn()) {
                            db.finishAttackingPhase();
                        } else if (newPhase == Database.Phase.MOVING && db.isThisPlayersTurn()) {
                            db.finishTurn();
                            turnsPlayed.addAndGet(1);
                        }
                    }
                } catch (Exception e) {
                    fail(e.getMessage());
                }
            });
            db.setNextTurnListener((playerID, isThisPlayer) -> {
                assertEquals(isThisPlayer, db.isThisPlayersTurn());
                nextTurnCount.addAndGet(1);
            });
        }

        DatabaseTestable.placeTurnArmies(DatabaseTestable.getClientToAct(dbs), 5000);

        Thread.sleep(1000 + 500*NUM_TURNS);
        assertEquals(NUM_TURNS*NUM_CLIENTS*3 + NUM_CLIENTS, phaseChangedCount.get());
        assertEquals(NUM_TURNS*NUM_CLIENTS, nextTurnCount.get());
    }

    @After
    public void teardown() {
        disconnectAll();
    }
}
