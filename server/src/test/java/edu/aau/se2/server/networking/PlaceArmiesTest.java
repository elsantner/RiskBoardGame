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

import edu.aau.se2.server.data.Lobby;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.data.Territory;
import edu.aau.se2.server.logic.ArmyCountHelper;
import edu.aau.se2.server.networking.dto.game.ArmyPlacedMessage;
import edu.aau.se2.server.networking.dto.game.CardExchangeMessage;
import edu.aau.se2.server.networking.dto.game.NewArmiesMessage;
import edu.aau.se2.server.networking.dto.game.NextTurnMessage;
import edu.aau.se2.server.networking.kryonet.NetworkClientKryo;

import static org.junit.Assert.assertEquals;

/**
 * Test placing armies at beginning of a turn.
 */
public class PlaceArmiesTest extends AbstractServerTest {
    private static final int NUM_CLIENTS = 3;

    private AtomicInteger newArmiesMsgCount = new AtomicInteger(0);
    private AtomicInteger armyPlacedMsgCount = new AtomicInteger(0);
    private AtomicInteger armiesPlaced = new AtomicInteger(0);

    private int lobbyID;
    private Map<NetworkClientKryo, Player> clientPlayers;
    private List<NetworkClientKryo> turnOrder;
    private Map<Integer, List<Territory>> playerOccupiedTerritories;

    public PlaceArmiesTest() {
        super(NUM_CLIENTS);
    }

    @Before
    public void setup() throws IOException, TimeoutException {
        // setup game until initial armies are placed
        server.start();
        clientPlayers = server.connect(Arrays.asList(clients), 5000);
        lobbyID = server.setupLobby(Arrays.asList(clients));
        turnOrder = server.startGame(lobbyID, clientPlayers);
        playerOccupiedTerritories = server.placeInitialArmies(lobbyID);
    }

    @Test
    public void testPlaceArmies() throws InterruptedException {
        Lobby l = server.getDataStore().getLobbyByID(lobbyID);
        Player playerToAct = clientPlayers.get(turnOrder.get(0));
        int armyCount = ArmyCountHelper.getNewArmyCount(l.getTerritoriesOccupiedByPlayer(playerToAct.getUid()), playerToAct.getUid());

        for (NetworkClientKryo client : clients) {
            client.registerCallback(msg -> {
                if (msg instanceof NewArmiesMessage) {
                    newArmiesMsgCount.addAndGet(1);
                    // if message is regarding this player
                    if (((NewArmiesMessage) msg).getFromPlayerID() == clientPlayers.get(client).getUid()) {
                        placeArmy(client);
                        armiesPlaced.addAndGet(1);
                    }
                } else if (msg instanceof ArmyPlacedMessage) {
                    armyPlacedMsgCount.addAndGet(1);
                    if (((ArmyPlacedMessage) msg).getFromPlayerID() == clientPlayers.get(client).getUid() &&
                            ((ArmyPlacedMessage) msg).getArmyCountRemaining() > 0) {
                        assertEquals(1, ((ArmyPlacedMessage) msg).getArmyCountPlaced());
                        assertEquals(armyCount-armiesPlaced.get(), ((ArmyPlacedMessage) msg).getArmyCountRemaining());
                        placeArmy(client);
                        armiesPlaced.addAndGet(1);
                    }
                }
            });
        }
        turnOrder.get(0).sendMessage(new CardExchangeMessage(lobbyID,
                clientPlayers.get(turnOrder.get(0)).getUid(), false));

        Thread.sleep(2000);
        // check that all messages were received by all clients
        assertEquals(armyCount*NUM_CLIENTS, armyPlacedMsgCount.get());
        assertEquals(NUM_CLIENTS, newArmiesMsgCount.get());
    }

    @After
    public void tearDown() {
        disconnectAll();
    }

    private void placeArmy(NetworkClientKryo client) {
        client.sendMessage(new ArmyPlacedMessage(lobbyID, clientPlayers.get(client).getUid(),
               playerOccupiedTerritories.get(clientPlayers.get(client).getUid()).get(0).getId(), 1));
    }
}
