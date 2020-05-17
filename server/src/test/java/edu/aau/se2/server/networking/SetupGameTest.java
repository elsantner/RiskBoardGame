package edu.aau.se2.server.networking;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.data.Territory;
import edu.aau.se2.server.logic.ArmyCountHelper;
import edu.aau.se2.server.networking.dto.game.ArmyPlacedMessage;
import edu.aau.se2.server.networking.dto.game.NextTurnMessage;
import edu.aau.se2.server.networking.kryonet.NetworkClientKryo;

import static org.junit.Assert.assertEquals;

/**
 * Test placing initial armies.
 */
public class SetupGameTest extends AbstractServerTest {
    private static final int NUM_CLIENTS = 3;

    private AtomicInteger armyPlacedMsgCount = new AtomicInteger(0);
    private AtomicBoolean nextTurnMsgReceived = new AtomicBoolean(false);

    private int lobbyID;
    private Map<NetworkClientKryo, Player> clientPlayers;
    private List<NetworkClientKryo> turnOrder;
    private ArrayList<Territory> unoccupiedTerritories;
    private Map<Integer, ArrayList<Territory>> playerOccupiedTerritories;
    private int currentTurnIndex = 0;

    public SetupGameTest() {
        super(NUM_CLIENTS);
    }

    @Before
    public void setup() throws IOException, TimeoutException {
        // setup game until game is started
        server.start();
        clientPlayers = server.connect(Arrays.asList(clients), 5000);
        lobbyID = server.setupLobby(Arrays.asList(clients));
        turnOrder = server.startGame(lobbyID, clientPlayers);
        setupTerritories();
    }

    private void setupTerritories() {
        playerOccupiedTerritories = new HashMap<>();
        for (Player p : clientPlayers.values()) {
            playerOccupiedTerritories.put(p.getUid(), new ArrayList<>());
        }
        unoccupiedTerritories = new ArrayList<>();
        for (int i=0; i<42; i++) {
            unoccupiedTerritories.add(new Territory(i));
        }
    }

    /**
     * Test that the turn order on server is the same as the one returned by server.startGame(..)
     */
    @Test
    public void testTurnOrder() {
        for (int i=0; i<NUM_CLIENTS; i++) {
            NetworkClientKryo client = turnOrder.get(i);
            assertEquals((int)server.getDataStore().getLobbyByID(lobbyID).getTurnOrder().get(i), clientPlayers.get(client).getUid());
        }
    }

    /**
     * Test the placing of initial armies
     * @throws InterruptedException
     */
    @Test
    public void testPlaceInitialArmies() throws InterruptedException {
        int initialArmyCountPerClient = ArmyCountHelper.getStartCount(NUM_CLIENTS);

        for (NetworkClientKryo client : clients) {
            client.registerCallback(msg -> {
                if (msg instanceof NextTurnMessage) {
                    nextTurnMsgReceived.set(true);
                } else if (msg instanceof ArmyPlacedMessage) {
                    // all clients count all army received messages
                    armyPlacedMsgCount.addAndGet(1);
                    // if it was this clients turn and not finished yet ...
                    if (((ArmyPlacedMessage) msg).getFromPlayerID() == clientPlayers.get(client).getUid() &&
                            !nextTurnMsgReceived.get()) {
                        // proceed to next client's turn
                        currentTurnIndex = (currentTurnIndex+1) % NUM_CLIENTS;
                        // check that player to act is in sync with server
                        assertEquals(clientPlayers.get(turnOrder.get(currentTurnIndex)).getUid(),
                                server.getDataStore().getLobbyByID(lobbyID).getPlayerToAct().getUid());
                        // let current client to act send army placing message
                        NetworkClientKryo currentClient = turnOrder.get(currentTurnIndex);
                        currentClient.sendMessage(new ArmyPlacedMessage(lobbyID, clientPlayers.get(currentClient).getUid(),
                                getNextTerritoryToPlaceArmy(clientPlayers.get(currentClient).getUid()), 1));
                    }
                }
            });
        }
        // first client to act kicks off above procedure
        turnOrder.get(0).sendMessage(new ArmyPlacedMessage(lobbyID, clientPlayers.get(turnOrder.get(0)).getUid(),
                getNextTerritoryToPlaceArmy(clientPlayers.get(turnOrder.get(0)).getUid()), 1));

        Thread.sleep(2000);
        // check that all army placing messages were received by all clients
        assertEquals(initialArmyCountPerClient*NUM_CLIENTS*NUM_CLIENTS, armyPlacedMsgCount.get());
    }

    @After
    public void tearDown() {
        disconnectAll();
    }

    private synchronized int getNextTerritoryToPlaceArmy(int clientPlayerID) {
        if (!unoccupiedTerritories.isEmpty()) {
            Territory t = unoccupiedTerritories.remove(0);
            playerOccupiedTerritories.get(clientPlayerID).add(t);
            return t.getId();
        }
        else {
            Random rand = new Random();
            ArrayList<Territory> thisPlayersTerritories = playerOccupiedTerritories.get(clientPlayerID);
            return thisPlayersTerritories.get(rand.nextInt(thisPlayersTerritories.size()-1)).getId();
        }
    }
}
