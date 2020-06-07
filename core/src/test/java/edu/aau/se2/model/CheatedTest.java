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
import edu.aau.se2.server.data.Territory;

import static org.junit.Assert.assertEquals;

public class CheatedTest extends AbstractDatabaseTest {
    private static final int NUM_CLIENTS = 3;
    private AtomicInteger countAttackResultMsg;

    public CheatedTest() {
        super(NUM_CLIENTS);
    }

    @Before
    public void setup() throws IOException, TimeoutException {
        server.start();
        setupScenario();
        countAttackResultMsg = new AtomicInteger(0);
    }

    private void setupScenario() throws IOException, TimeoutException {
        DatabaseTestable.setupLobby(dbs, 5000);
        DatabaseTestable.startLobby(dbs, 5000);
        DatabaseTestable.setupGame(dbs, 5000);
        DatabaseTestable.placeTurnArmies(DatabaseTestable.getClientToAct(dbs), 5000);
    }


    @Test
    public void testCheating() throws InterruptedException {
        for (DatabaseTestable db : dbs) {
            db.getListeners().setAttackUpdatedListener(new OnAttackUpdatedListener() {
                @Override
                public void attackStarted() {
                }

                @Override
                public void attackUpdated() {
                }

                @Override
                public void attackFinished() {
                    countAttackResultMsg.addAndGet(1);
                }
            });
        }

        DatabaseTestable clientToAct = DatabaseTestable.getClientToAct(dbs);
        Territory fromTerritory = clientToAct.getMyTerritory(2);
        DatabaseTestable defenderClient = DatabaseTestable.getDifferentClient(dbs, clientToAct);
        Territory toTerritory = defenderClient.getMyTerritory(2);
        clientToAct.startAttack(fromTerritory.getId(), toTerritory.getId(), 1);

        List<Integer> attackerResults = new ArrayList<>();
        attackerResults.add(5);
        clientToAct.sendAttackerResults(attackerResults, false);

        Thread.sleep(1500);

        defenderClient.accuseCheater();

        Thread.sleep(1000);

        assertEquals(NUM_CLIENTS, countAttackResultMsg.get());
    }

    @After
    public void teardown() {
        disconnectAll();
    }
    }

