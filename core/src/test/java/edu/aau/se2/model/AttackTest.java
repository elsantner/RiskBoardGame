package edu.aau.se2.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import edu.aau.se2.server.data.Territory;
import edu.aau.se2.server.networking.MainServerTestable;

import static org.junit.Assert.assertEquals;

public class AttackTest extends AbstractDatabaseTest {
    private static final int NUM_CLIENTS = 3;

    public AttackTest() {
        super(NUM_CLIENTS);
    }

    @Before
    public void setup() throws IOException, TimeoutException {
        server.start();
        setupScenario();
    }

    private void setupScenario() throws IOException, TimeoutException {
        DatabaseTestable.setupLobby(dbs, 5000);
        DatabaseTestable.setupGame(dbs, 5000);
        DatabaseTestable.placeTurnArmies(DatabaseTestable.getClientToAct(dbs), 5000);
    }

    @Test
    public void testStartAttack() throws InterruptedException {
        DatabaseTestable clientToAct = DatabaseTestable.getClientToAct(dbs);
        Territory fromTerritory = clientToAct.getMyTerritory(2);
        Territory toTerritory = DatabaseTestable.getDifferentClient(dbs, clientToAct).getMyTerritories().get(0);
        clientToAct.attackStarted(fromTerritory.getId(), toTerritory.getId(), 1);

        Thread.sleep(2000);
        for (DatabaseTestable db : dbs) {
            assertEquals(fromTerritory.getId(), db.getAttack().getFromTerritoryID());
            assertEquals(toTerritory.getId(), db.getAttack().getToTerritoryID());
            assertEquals(1, db.getAttack().getAttackerDiceCount());
            assertEquals(Database.Phase.ATTACKING, db.getCurrentPhase());
        }
    }

    @After
    public void teardown() {
        disconnectAll();
    }
}
