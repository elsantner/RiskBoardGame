package edu.aau.se2.server.networking;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.logic.ArmyCountHelper;
import edu.aau.se2.server.networking.dto.game.InitialArmyPlacingMessage;
import edu.aau.se2.server.networking.dto.game.StartGameMessage;
import edu.aau.se2.server.networking.dto.lobby.PlayersChangedMessage;
import edu.aau.se2.server.networking.dto.lobby.ReadyMessage;
import edu.aau.se2.server.networking.kryonet.NetworkClientKryo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test starting a game by signaling ready status.
 */
public class StartLobbyTest extends AbstractServerTest {
    private static final int NUM_CLIENTS = 3;

    private AtomicInteger playersChangedMsgCount = new AtomicInteger(0);
    private AtomicInteger startGameTestMsgCount = new AtomicInteger(0);
    private AtomicInteger initArmyPlacingMsgCount = new AtomicInteger(0);

    private int lobbyID;
    private Map<NetworkClientKryo, Player> clientPlayers;

    public StartLobbyTest() {
        super(NUM_CLIENTS);
    }

    @Before
    public void setup() throws IOException, TimeoutException {
        // setup clients in lobby
        server.start();
        clientPlayers = server.connect(Arrays.asList(clients), 5000);
        lobbyID = server.setupLobby(Arrays.asList(clients));
    }

    /**
     * Test the toggling of ready status.
     * @throws InterruptedException
     */
    @Test
    public void testToggleReady() throws InterruptedException {
        int id = clientPlayers.get(clients[0]).getUid();

        for (NetworkClientKryo client : clients) {
            client.registerCallback(msg -> {
                if (msg instanceof PlayersChangedMessage) {
                    playersChangedMsgCount.addAndGet(1);
                }
            });
        }
        clients[0].sendMessage(new ReadyMessage(lobbyID, id, true));
        Thread.sleep(1000);
        assertTrue(server.getDataStore().getPlayerByID(id).isReady());
        clients[0].sendMessage(new ReadyMessage(lobbyID, id, false));
        Thread.sleep(1000);
        assertFalse(server.getDataStore().getPlayerByID(id).isReady());
        // check that all status updates were received by all clients
        assertEquals(NUM_CLIENTS*2, playersChangedMsgCount.get());
    }

    /**
     * Test the starting of the game if all clients are ready.
     * @throws InterruptedException
     */
    @Test
    public void testStartLobby() throws InterruptedException {
        for (NetworkClientKryo client : clients) {
            client.registerCallback(msg -> {
                if (msg instanceof StartGameMessage) {
                    // check that the right start army count was provided
                    assertEquals(ArmyCountHelper.getStartCount(NUM_CLIENTS), ((StartGameMessage) msg).getStartArmyCount());
                    startGameTestMsgCount.addAndGet(1);
                } else if (msg instanceof InitialArmyPlacingMessage) {
                    initArmyPlacingMsgCount.addAndGet(1);
                }
            });
            client.sendMessage(new ReadyMessage(lobbyID, clientPlayers.get(client).getUid(), true));
        }
        Thread.sleep(1000);
        assertEquals(NUM_CLIENTS, startGameTestMsgCount.get());
        Thread.sleep(2000);
        assertEquals(NUM_CLIENTS, initArmyPlacingMsgCount.get());
    }

    @After
    public void tearDown() {
        disconnectAll();
    }
}
