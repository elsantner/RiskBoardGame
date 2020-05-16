package edu.aau.se2.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import edu.aau.se2.server.logic.ArmyCountHelper;

import static org.junit.Assert.*;

public class StartLobbyTest extends AbstractDatabaseTest {
    private static final int NUM_CLIENTS = 3;

    private AtomicInteger gameStartedCount;

    public StartLobbyTest() {
        super(NUM_CLIENTS);
    }

    @Before
    public void setup() throws IOException, TimeoutException {
        gameStartedCount = new AtomicInteger(0);
        server.start();
        setupScenario();
    }

    private void setupScenario() throws IOException, TimeoutException {
        DatabaseTestable.setupLobby(dbs, 5000);
    }

    /**
     * Test starting of lobby if all players set ready status.
     * @throws InterruptedException
     */
    @Test
    public void testStartLobby() throws InterruptedException {
        for (DatabaseTestable db : dbs) {
            db.setGameStartListener((players, initialArmyCount) -> gameStartedCount.addAndGet(1));
            db.setPlayerReady(true);
        }

        Thread.sleep(2000);
        assertEquals(NUM_CLIENTS, gameStartedCount.get());
        for (DatabaseTestable db : dbs) {
            assertEquals(ArmyCountHelper.getStartCount(NUM_CLIENTS), db.getCurrentArmyReserve());
            assertFalse(db.isInitialArmyPlacementFinished());
        }
    }

    @After
    public void teardown() {
        disconnectAll();
    }
}
