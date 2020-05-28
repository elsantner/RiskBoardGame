package edu.aau.se2.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import edu.aau.se2.model.listener.OnAttackUpdatedListener;
import edu.aau.se2.server.data.Territory;

import static org.junit.Assert.*;

public class AttackTest extends AbstractDatabaseTest {
    private static final int NUM_CLIENTS = 3;

    private AtomicInteger attackStartedCount;
    private AtomicInteger defenderCount;

    public AttackTest() {
        super(NUM_CLIENTS);
    }

    @Before
    public void setup() throws IOException, TimeoutException {
        attackStartedCount = new AtomicInteger(0);
        defenderCount = new AtomicInteger(0);
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
     * Test starting of an attack.
     * @throws InterruptedException
     */
    @Test
    public void testStartAttack() throws InterruptedException {
        for (DatabaseTestable db : dbs) {
            db.getListeners().setAttackUpdatedListener(new OnAttackUpdatedListener() {
                @Override
                public void attackStarted() {
                    attackStartedCount.addAndGet(1);
                    if (db.isThisPlayerDefender()) {
                        defenderCount.addAndGet(1);
                    }
                }

                @Override
                public void attackUpdated() {
                    // unused
                }

                @Override
                public void attackFinished() {
                    // unused
                }
            });
        }

        DatabaseTestable clientToAct = DatabaseTestable.getClientToAct(dbs);
        DatabaseTestable defender = DatabaseTestable.getDifferentClient(dbs, clientToAct);
        Territory fromTerritory = clientToAct.getMyTerritory(2);
        Territory toTerritory = defender.getMyTerritories()[0];
        // is defender must be false before attack start
        assertFalse(defender.isThisPlayerDefender());
        clientToAct.startAttack(fromTerritory.getId(), toTerritory.getId(), 1);

        Thread.sleep(2000);
        for (DatabaseTestable db : dbs) {
            assertEquals(fromTerritory.getId(), db.getLobby().getCurrentAttack().getFromTerritoryID());
            assertEquals(toTerritory.getId(), db.getLobby().getCurrentAttack().getToTerritoryID());
            assertEquals(1, db.getLobby().getCurrentAttack().getAttackerDiceCount());
            assertEquals(Database.Phase.ATTACKING, db.getCurrentPhase());
        }
        assertEquals(NUM_CLIENTS, attackStartedCount.get());
        assertEquals(1, defenderCount.get());
        assertTrue(defender.isThisPlayerDefender());
    }

    @After
    public void teardown() {
        disconnectAll();
    }
}
