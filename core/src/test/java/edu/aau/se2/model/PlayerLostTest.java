package edu.aau.se2.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import edu.aau.se2.model.listener.OnAttackUpdatedListener;
import edu.aau.se2.model.listener.OnPlayerLostListener;
import edu.aau.se2.server.data.Lobby;
import edu.aau.se2.server.data.Territory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PlayerLostTest extends AbstractDatabaseTest {

    private static final int NUM_CLIENTS = 3;

    private AtomicInteger attackFinishedCount;
    private AtomicInteger territoryUpdateCount;
    private AtomicInteger playerLostCount;

    public PlayerLostTest() {
        super(NUM_CLIENTS);
    }

    @Before
    public void setup() throws IOException, TimeoutException {
        attackFinishedCount = new AtomicInteger(0);
        territoryUpdateCount = new AtomicInteger(0);
        playerLostCount = new AtomicInteger(0);
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
     * Test that player loses after his last territory is occupied
     *
     * @throws InterruptedException
     */
    @Test
    public void testPlayerLost() throws InterruptedException {
        DatabaseTestable clientToAct = DatabaseTestable.getClientToAct(dbs);
        Territory fromTerritory = clientToAct.getMyTerritory(2);
        DatabaseTestable otherClient = DatabaseTestable.getDifferentClient(dbs, clientToAct);
        Territory toTerritory = otherClient.getMyTerritories()[0];

        Lobby l = server.getDataStore().getLobbyByID(dbs[0].getLobby().getLobbyID());
        Territory[] territoriesOfLoser = l.getTerritoriesOccupiedByPlayer(otherClient.getThisPlayer().getUid());

        for (Territory territory : territoriesOfLoser) {
            if (territory.getId() != otherClient.getMyTerritories()[0].getId()) {
                territory.setOccupierPlayerID(-1);
            }
        }


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
                    assertNull(db.getLobby().getCurrentAttack());
                    attackFinishedCount.addAndGet(1);
                }
            });
            db.getListeners().setTerritoryUpdateListener((territoryID, armyCount, colorID) -> territoryUpdateCount.addAndGet(1));
            db.getListeners().setPlayerLostListener(new OnPlayerLostListener() {
                @Override
                public void informPlayersThatPlayerLost(String playerName, boolean thisPlayerLost) {
                    playerLostCount.addAndGet(1);
                }
            });
        }

        server.setupPreOccupy(dbs[0].getLobby().getLobbyID(), fromTerritory.getId(), toTerritory.getId());
        clientToAct.occupyTerritory(toTerritory.getId(), fromTerritory.getId(), 1);

        Thread.sleep(2000);
        assertEquals(NUM_CLIENTS, attackFinishedCount.get());
        assertEquals(NUM_CLIENTS * 2, territoryUpdateCount.get());
        assertEquals(NUM_CLIENTS, playerLostCount.get());
    }

    @After
    public void teardown() {
        disconnectAll();
    }
}

