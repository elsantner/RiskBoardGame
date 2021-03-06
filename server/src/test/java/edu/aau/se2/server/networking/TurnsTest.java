package edu.aau.se2.server.networking;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import edu.aau.se2.server.data.Lobby;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.networking.dto.game.AttackingPhaseFinishedMessage;
import edu.aau.se2.server.networking.dto.game.NextTurnMessage;
import edu.aau.se2.server.networking.kryonet.NetworkClientKryo;

import static org.junit.Assert.assertEquals;

/**
 * Test playing and ending turns.
 */
public class TurnsTest extends AbstractServerTest {
    private static final int NUM_CLIENTS = 3;
    private static final int NUM_TURNS = 7;

    private AtomicInteger nextTurnMsgCount;
    private AtomicInteger nextTurnMsgsSent;
    private AtomicInteger attackingPhaseFinishedMsgCount;

    private int lobbyID;
    private Map<NetworkClientKryo, Player> clientPlayers;
    private List<NetworkClientKryo> turnOrder;

    public TurnsTest() {
        super(NUM_CLIENTS);
    }

    @Before
    public void setup() throws IOException, TimeoutException {
        nextTurnMsgCount = new AtomicInteger(0);
        nextTurnMsgsSent = new AtomicInteger(0);
        attackingPhaseFinishedMsgCount = new AtomicInteger(0);
        // setup game until initial armies are placed
        server.start();
        clientPlayers = server.connect(Arrays.asList(clients), 5000);
        lobbyID = server.setupLobby(Arrays.asList(clients));
        turnOrder = server.startGame(lobbyID, clientPlayers);
        server.placeInitialArmies(lobbyID);
    }

    /**
     * Test if turns work correctly.
     * @throws InterruptedException
     */
    @Test
    public void testPlayTurns() throws InterruptedException {
        Lobby l = server.getDataStore().getLobbyByID(lobbyID);
        server.setTurnArmiesPlaced(lobbyID);

        for (NetworkClientKryo client : clients) {
            client.registerCallback(msg -> {
                if (msg instanceof NextTurnMessage) {
                    assertEquals(l.getPlayerToAct().getUid(), ((NextTurnMessage) msg).getPlayerToActID());
                    nextTurnMsgCount.addAndGet(1);
                    // if another turn is to be played and it's this players turn
                    if (nextTurnMsgsSent.get() < NUM_TURNS &&
                            ((NextTurnMessage) msg).getPlayerToActID() == clientPlayers.get(client).getUid()) {

                        server.setTurnArmiesPlaced(lobbyID);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        client.sendMessage(new NextTurnMessage(lobbyID, clientPlayers.get(client).getUid()));
                        nextTurnMsgsSent.addAndGet(1);
                    }
                }
            });
        }

        turnOrder.get(0).sendMessage(new NextTurnMessage(lobbyID,
                clientPlayers.get(turnOrder.get(0)).getUid()));
        nextTurnMsgsSent.addAndGet(1);

        Thread.sleep(1000+500*NUM_TURNS);
        // check that all messages were received by all clients
        assertEquals(NUM_CLIENTS*NUM_TURNS, nextTurnMsgCount.get());
    }

    /**
     * Test if skipping attack phase is correctly propagated to all clients in lobby.
     * @throws InterruptedException
     */
    @Test
    public void testSkipAttackingPhase() throws InterruptedException {
        Lobby l = server.getDataStore().getLobbyByID(lobbyID);
        server.setTurnArmiesPlaced(lobbyID);

        for (NetworkClientKryo client : clients) {
            client.registerCallback(msg -> {
                if (msg instanceof AttackingPhaseFinishedMessage) {
                    assertEquals(l.getPlayerToAct().getUid(), ((AttackingPhaseFinishedMessage) msg).getFromPlayerID());
                    attackingPhaseFinishedMsgCount.addAndGet(1);
                }
            });
        }
        turnOrder.get(0).sendMessage(new AttackingPhaseFinishedMessage(lobbyID,
                clientPlayers.get(turnOrder.get(0)).getUid()));

        Thread.sleep(1000);
        // check that all messages were received by all clients
        assertEquals(NUM_CLIENTS, attackingPhaseFinishedMsgCount.get());
    }

    @After
    public void tearDown() {
        disconnectAll();
    }
}
