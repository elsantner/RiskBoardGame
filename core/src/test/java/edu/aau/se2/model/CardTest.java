package edu.aau.se2.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import edu.aau.se2.model.listener.OnCardsChangedListener;

import static org.junit.Assert.assertEquals;

public class CardTest extends AbstractDatabaseTest {
    private static final int NUM_CLIENTS = 3;
    private AtomicInteger nextTurnCount;
    private AtomicInteger newCardCount;

    public CardTest() {
        super(NUM_CLIENTS);
    }

    @Before
    public void setup() throws IOException, TimeoutException {

        nextTurnCount = new AtomicInteger(0);
        newCardCount = new AtomicInteger(0);
        server.start();
        setupScenario();
    }

    private void setupScenario() throws IOException, TimeoutException {
        DatabaseTestable.setupLobby(dbs, 5000);
        DatabaseTestable.startLobby(dbs, 5000);
        DatabaseTestable.setupGame(dbs, 5000);
        DatabaseTestable.placeTurnArmies(DatabaseTestable.getClientToAct(dbs), 5000);
    }

    @Test
    public void testNewCardMessage() throws InterruptedException {
        DatabaseTestable clientToAct = DatabaseTestable.getClientToAct(dbs);

        for (DatabaseTestable db : dbs) {
            db.getListeners().setCardsChangedListener((new OnCardsChangedListener() {
                @Override
                public void singleNewCard(String cardName) {
                    assertEquals('c', (cardName.charAt(0)));
                    assertEquals('_', (cardName.charAt(4)));
                    newCardCount.addAndGet(1);
                }

                @Override
                public void refreshCards(String[] cardNames) {
                    //unused
                }
            }));
            db.getListeners().setNextTurnListener((playerID, isThisPlayer) -> nextTurnCount.addAndGet(1));
        }
        clientToAct.finishAttackingPhase();
        clientToAct.finishTurn();

        Thread.sleep(2000);

        assertEquals(NUM_CLIENTS, nextTurnCount.get());
        assertEquals(1, newCardCount.get());
    }

    @After
    public void teardown() {
        disconnectAll();
    }
}

