package edu.aau.se2.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import edu.aau.se2.model.listener.OnAttackUpdatedListener;
import edu.aau.se2.model.listener.OnTerritoryUpdateListener;
import edu.aau.se2.server.data.Territory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class OccupyTest extends AbstractDatabaseTest {
    private static final int NUM_CLIENTS = 3;

    private AtomicInteger attackFinishedCount;
    private AtomicInteger territoryUpdateCount;

    public OccupyTest() {
        super(NUM_CLIENTS);
    }

    @Before
    public void setup() throws IOException, TimeoutException {
        attackFinishedCount = new AtomicInteger(0);
        territoryUpdateCount = new AtomicInteger(0);
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
     * Test occupation after an attack.
     * @throws InterruptedException
     */
    @Test
    public void testOccupyTerritory() throws InterruptedException {
        for (DatabaseTestable db : dbs) {
            db.getListeners().setAttackUpdatedListener(new OnAttackUpdatedListener() {
                @Override
                public void attackStarted() {
                    // unused
                }

                @Override
                public void attackUpdated() {
                    // unused
                }

                @Override
                public void attackFinished() {
                    assertNull(db.getAttack());
                    attackFinishedCount.addAndGet(1);
                }
            });
            db.getListeners().setTerritoryUpdateListener((territoryID, armyCount, colorID) -> territoryUpdateCount.addAndGet(1));
        }

        DatabaseTestable clientToAct = DatabaseTestable.getClientToAct(dbs);
        Territory fromTerritory = clientToAct.getMyTerritory(2);
        Territory toTerritory = DatabaseTestable.getDifferentClient(dbs, clientToAct).getMyTerritories().get(0);

        server.setupPreOccupy(dbs[0].getCurrentLobbyID(), fromTerritory.getId(), toTerritory.getId());
        clientToAct.occupyTerritory(toTerritory.getId(), fromTerritory.getId(), 1);

        Thread.sleep(2000);
        assertEquals(NUM_CLIENTS, attackFinishedCount.get());
        assertEquals(NUM_CLIENTS*2, territoryUpdateCount.get());
    }

    @After
    public void teardown() {
        disconnectAll();
    }
}
