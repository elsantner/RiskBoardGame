package edu.aau.se2.server.networking;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import edu.aau.se2.server.data.Lobby;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.networking.dto.game.AttackStartedMessage;
import edu.aau.se2.server.networking.kryonet.NetworkClientKryo;

import static org.junit.Assert.*;

/**
 * Test starting attacks on territories.
 */
public class AttackStartTest extends AbstractServerTest {
    private static final int NUM_CLIENTS = 3;

    private AtomicInteger countAttackStartedMsgsReceived1 = new AtomicInteger(0);
    private AtomicInteger countAttackStartedMsgsReceived2 = new AtomicInteger(0);
    private AtomicInteger countAttackStartedMsgsReceived3 = new AtomicInteger(0);

    private int lobbyID;
    private List<Integer> turnOrder;
    private Map<Integer, Player> players;

    public AttackStartTest() {
        super(NUM_CLIENTS);
    }

    @Before
    public void setup() throws IOException, TimeoutException {
        // setup game until initial armies are placed
        server.start();
        server.connect(Arrays.asList(clients), 5000);
        lobbyID = server.setupLobby(Arrays.asList(clients));
        turnOrder = server.setupGame(lobbyID);

        players = new HashMap<>();
        for (Player p : server.getDataStore().getLobbyByID(lobbyID).getPlayers()) {
            players.put(p.getUid(), p);
        }
    }

    /**
     * Test the starting of an attack by the player to act on an opponents territory.
     * @throws InterruptedException
     */
    @Test
    public void testStartAttack() throws InterruptedException {
        Player attacker = players.get(turnOrder.get(0));
        for (NetworkClientKryo client : clients) {
            client.registerCallback(msg -> {
                if (msg instanceof AttackStartedMessage) {
                    countAttackStartedMsgsReceived1.addAndGet(1);
                }
            });
        }

        Lobby l = server.getDataStore().getLobbyByID(lobbyID);
        clients[0].sendMessage(new AttackStartedMessage(lobbyID, attacker.getUid(),
                l.getTerritoriesOccupiedByPlayer(attacker.getUid())[0].getId(),
                l.getTerritoriesOccupiedByPlayer(players.get(turnOrder.get(1)).getUid())[0].getId(), 1));

        Thread.sleep(1500);
        // check that all clients received the message
        assertEquals(NUM_CLIENTS, countAttackStartedMsgsReceived1.get());
    }

    /**
     * Test the starting of an attack by a player who's turn it is NOT. (Should not work.)
     * @throws InterruptedException
     */
    @Test
    public void testStartAttackWrongTurn() throws InterruptedException {
        Player attacker = players.get(turnOrder.get(1));
        for (NetworkClientKryo client : clients) {
            client.registerCallback(msg -> {
                if (msg instanceof AttackStartedMessage) {
                    countAttackStartedMsgsReceived2.addAndGet(1);
                }
            });
        }
        clients[0].sendMessage(new AttackStartedMessage(lobbyID, attacker.getUid(), 0, 1, 1));

        Thread.sleep(1500);
        assertEquals(0, countAttackStartedMsgsReceived2.get());
    }

    /**
     * Test the starting of an attack by the player to act from a territory to the same territory. (Should not work.)
     * @throws InterruptedException
     */
    @Test
    public void testStartAttackWrongTerritory() throws InterruptedException {
        Player attacker = players.get(turnOrder.get(0));
        for (NetworkClientKryo client : clients) {
            client.registerCallback(msg -> {
                if (msg instanceof AttackStartedMessage) {
                    countAttackStartedMsgsReceived3.addAndGet(1);
                }
            });
        }
        clients[0].sendMessage(new AttackStartedMessage(lobbyID, attacker.getUid(), 1, 1, 1));

        Thread.sleep(1500);
        assertEquals(0, countAttackStartedMsgsReceived3.get());
    }

    @After
    public void tearDown() {
        disconnectAll();
    }
}
