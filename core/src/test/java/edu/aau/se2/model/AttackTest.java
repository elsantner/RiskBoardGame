package edu.aau.se2.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import edu.aau.se2.model.listener.OnAttackUpdatedListener;
import edu.aau.se2.server.data.Attack;
import edu.aau.se2.server.data.Territory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AttackTest extends AbstractDatabaseTest {
    private static final int NUM_CLIENTS = 3;

    private AtomicInteger attackStartedCount;
    private AtomicInteger defenderCount;
    private AtomicInteger attackerResultCount;
    private AtomicInteger attackUpdatedCount;

    public AttackTest() {
        super(NUM_CLIENTS);
    }

    @Before
    public void setup() throws IOException, TimeoutException {
        attackStartedCount = new AtomicInteger(0);
        defenderCount = new AtomicInteger(0);
        attackerResultCount = new AtomicInteger(0);
        attackUpdatedCount = new AtomicInteger(0);
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

    @Test
    public void testAttackerResult() throws InterruptedException {
        for (DatabaseTestable db : dbs) {
            db.getListeners().setAttackUpdatedListener(new OnAttackUpdatedListener() {
                @Override
                public void attackStarted() {

                }

                @Override
                public void attackUpdated() {
                    attackerResultCount.addAndGet(1);
                }

                @Override
                public void attackFinished() {
                    // unused
                }
            });
        }

        DatabaseTestable clientToAct = DatabaseTestable.getClientToAct(dbs);
        Territory fromTerritory = clientToAct.getMyTerritory(2);
        Territory toTerritory = DatabaseTestable.getDifferentClient(dbs, clientToAct).getMyTerritories()[0];
        clientToAct.startAttack(fromTerritory.getId(), toTerritory.getId(), 1);

        List<Integer> attackerResults = new ArrayList<>();
        attackerResults.add(5);
        clientToAct.sendAttackerResults(attackerResults, false);

        Thread.sleep(2000);
        for (DatabaseTestable db : dbs) {
            assertEquals(fromTerritory.getId(), db.getLobby().getCurrentAttack().getFromTerritoryID());
            assertEquals(toTerritory.getId(), db.getLobby().getCurrentAttack().getToTerritoryID());
            assertEquals(1, db.getLobby().getCurrentAttack().getAttackerDiceCount());
            assertEquals(attackerResults, db.getLobby().getCurrentAttack().getAttackerDiceResults());
            assertEquals(Database.Phase.ATTACKING, db.getCurrentPhase());
        }
        assertEquals(NUM_CLIENTS, attackerResultCount.get());
    }

    @Test
    public void testCompleteAttackPhase() throws InterruptedException {
        Attack[] attack = new Attack[NUM_CLIENTS];

        for (int i = 0; i < NUM_CLIENTS; i++) {
            int finalI = i;
            dbs[i].getListeners().setAttackUpdatedListener(new OnAttackUpdatedListener() {
                @Override
                public void attackStarted() {

                }

                @Override
                public void attackUpdated() {
                    attackUpdatedCount.addAndGet(1);
                }

                @Override
                public void attackFinished() {
                    attack[finalI] = dbs[finalI].getLobby().getCurrentAttack();
                }
            });
        }

        DatabaseTestable clientToAct = DatabaseTestable.getClientToAct(dbs);
        DatabaseTestable defenderClient = DatabaseTestable.getDifferentClient(dbs, clientToAct);
        Territory fromTerritory = clientToAct.getMyTerritory(2);
        Territory toTerritory = defenderClient.getMyTerritories()[0];
        clientToAct.startAttack(fromTerritory.getId(), toTerritory.getId(), 1);

        List<Integer> attackerResults = new ArrayList<>();
        attackerResults.add(5);
        clientToAct.sendAttackerResults(attackerResults, false);

        Thread.sleep(2000);

        defenderClient.sendDefenderDiceCount(1);
        List<Integer> defenderResults = new ArrayList<>();
        defenderResults.add(5);
        defenderClient.sendDefenderResults(defenderResults);



        Thread.sleep(5000);
        for (int i = 0; i < NUM_CLIENTS; i++) {
            assertEquals(fromTerritory.getId(), attack[i].getFromTerritoryID());
            assertEquals(toTerritory.getId(), attack[i].getToTerritoryID());
            assertEquals(1, attack[i].getAttackerDiceCount());
            assertEquals(attackerResults, attack[i].getAttackerDiceResults());
            assertEquals(1, attack[i].getDefenderDiceCount());
            assertEquals(defenderResults, attack[i].getDefenderDiceResults());
            assertEquals(1, attack[i].getArmiesLostAttacker());
            assertEquals(0, attack[i].getArmiesLostDefender());
            assertEquals(Database.Phase.ATTACKING, dbs[i].getCurrentPhase());
        }
        assertEquals(NUM_CLIENTS * 4, attackUpdatedCount.get());
        Thread.sleep(4000);
    }

    @After
    public void teardown() {
        disconnectAll();
    }
}
