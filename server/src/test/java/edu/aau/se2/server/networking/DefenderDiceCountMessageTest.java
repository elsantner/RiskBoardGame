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
import edu.aau.se2.server.data.Territory;
import edu.aau.se2.server.networking.dto.game.AttackStartedMessage;
import edu.aau.se2.server.networking.dto.game.DefenderDiceCountMessage;
import edu.aau.se2.server.networking.kryonet.NetworkClientKryo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DefenderDiceCountMessageTest extends AbstractServerTest {
    private static final int NUM_CLIENTS = 3;

    private AtomicInteger countDefenderDiceCountMsgsReceived1 = new AtomicInteger(0);
    private AtomicInteger countDefenderDiceCountMsgsReceived2 = new AtomicInteger(0);
    private AtomicInteger countDefenderDiceCountMsgsReceived3 = new AtomicInteger(0);

    private Map<NetworkClientKryo, Player> clientPlayers;
    private int lobbyID;
    private List<NetworkClientKryo> turnOrder ;

    private Map<Integer, List<Territory>> playerOccupiedTerritories;

    public DefenderDiceCountMessageTest() {
        super(NUM_CLIENTS);
    }

    @Before
    public void setup() throws IOException, InterruptedException, TimeoutException {
        // setup game until initial armies are placed
        server.start();
        clientPlayers = server.connect(Arrays.asList(clients), 5000);
        lobbyID = server.setupLobby(Arrays.asList(clients));
        turnOrder = server.startGame(lobbyID, clientPlayers);

        playerOccupiedTerritories = server.placeInitialArmies(lobbyID);

        Player attacker = clientPlayers.get(turnOrder.get(0));
        Player defender = clientPlayers.get(turnOrder.get(1));

        clients[0].sendMessage(new AttackStartedMessage(lobbyID, attacker.getUid(),
                playerOccupiedTerritories.get(attacker.getUid()).get(0).getId(),
                playerOccupiedTerritories.get(defender.getUid()).get(0).getId(), 1));

        Thread.sleep(1500);
    }

    @Test
    public void testDefenderDiceCount() throws InterruptedException {
        Player defender = clientPlayers.get(turnOrder.get(1));
        for (NetworkClientKryo client : clients) {
            client.registerCallback(msg -> {
                if (msg instanceof DefenderDiceCountMessage) {
                    countDefenderDiceCountMsgsReceived1.addAndGet(1);
                }
            });
        }

        Lobby l = server.getDataStore().getLobbyByID(lobbyID);
        assertTrue(l.attackRunning());
        clients[0].sendMessage(new DefenderDiceCountMessage(lobbyID, defender.getUid(), 1));

        Thread.sleep(1500);
        assertEquals(NUM_CLIENTS, countDefenderDiceCountMsgsReceived1.get());
    }

    @Test
    public void testDefenderWrongDiceCount() throws InterruptedException {
        Player defender = clientPlayers.get(turnOrder.get(1));
        for (NetworkClientKryo client : clients) {
            client.registerCallback(msg -> {
                if (msg instanceof DefenderDiceCountMessage) {
                    countDefenderDiceCountMsgsReceived2.addAndGet(1);
                }
            });
        }

        Lobby l = server.getDataStore().getLobbyByID(lobbyID);
        assertTrue(l.attackRunning());
        clients[0].sendMessage(new DefenderDiceCountMessage(lobbyID, defender.getUid(), 3));

        Thread.sleep(1500);
        assertEquals(0, countDefenderDiceCountMsgsReceived2.get());
    }

    @Test
    public void testNonDefenderDiceCount() throws InterruptedException {
        Player nonDefender = clientPlayers.get(turnOrder.get(2));
        for (NetworkClientKryo client : clients) {
            client.registerCallback(msg -> {
                if (msg instanceof DefenderDiceCountMessage) {
                    countDefenderDiceCountMsgsReceived3.addAndGet(1);
                }
            });
        }

        Lobby l = server.getDataStore().getLobbyByID(lobbyID);
        assertTrue(l.attackRunning());
        clients[0].sendMessage(new DefenderDiceCountMessage(lobbyID, nonDefender.getUid(), 1));

        Thread.sleep(1500);
        assertEquals(0, countDefenderDiceCountMsgsReceived3.get());
    }

    @After
    public void tearDown() {
        disconnectAll();
    }
}
