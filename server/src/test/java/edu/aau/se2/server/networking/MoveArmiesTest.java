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
import edu.aau.se2.server.logic.ArmyCountHelper;
import edu.aau.se2.server.networking.dto.game.ArmyMovedMessage;
import edu.aau.se2.server.networking.dto.game.ArmyPlacedMessage;
import edu.aau.se2.server.networking.dto.game.CardExchangeMessage;
import edu.aau.se2.server.networking.dto.game.NewArmiesMessage;
import edu.aau.se2.server.networking.dto.game.NextTurnMessage;
import edu.aau.se2.server.networking.kryonet.NetworkClientKryo;

import static org.junit.Assert.assertEquals;

/**
 * Test moving armies at the end of a turn.
 */
public class MoveArmiesTest extends AbstractServerTest {
    private static final int NUM_CLIENTS = 3;

    private AtomicInteger armyMovedMsgCount;
    private AtomicInteger nextTurnMsgCount;

    private int lobbyID;
    private Map<NetworkClientKryo, Player> clientPlayers;
    private List<NetworkClientKryo> turnOrder;
    private Map<Integer, List<Territory>> playerOccupiedTerritories;

    public MoveArmiesTest() {
        super(NUM_CLIENTS);
    }

    @Before
    public void setup() throws IOException, TimeoutException {
        armyMovedMsgCount = new AtomicInteger(0);
        nextTurnMsgCount = new AtomicInteger(0);
        // setup game until initial armies are placed
        server.start();
        clientPlayers = server.connect(Arrays.asList(clients), 5000);
        lobbyID = server.setupLobby(Arrays.asList(clients));
        turnOrder = server.startGame(lobbyID, clientPlayers);
        playerOccupiedTerritories = server.placeInitialArmies(lobbyID);
        server.setTurnArmiesPlaced(lobbyID);
    }

    /**
     * Test if moving armies by player to act is successful.
     * @throws InterruptedException
     */
    @Test
    public void testMoveArmies() throws InterruptedException {
        Lobby l = server.getDataStore().getLobbyByID(lobbyID);
        Player playerToAct = clientPlayers.get(turnOrder.get(0));
        List<Territory> territoriesOfPlayer = playerOccupiedTerritories.get(playerToAct.getUid());
        Territory fromTerritory = territoriesOfPlayer.get(0);
        Territory toTerritory = territoriesOfPlayer.get(1);
        int armyCountMoved = fromTerritory.getArmyCount() - 1;

        for (NetworkClientKryo client : clients) {
            client.registerCallback(msg -> {
                if (msg instanceof ArmyMovedMessage) {
                    assertEquals(armyCountMoved, ((ArmyMovedMessage) msg).getArmyCountMoved());
                    assertEquals(fromTerritory.getId(), ((ArmyMovedMessage) msg).getFromTerritoryID());
                    assertEquals(toTerritory.getId(), ((ArmyMovedMessage) msg).getToTerritoryID());
                    armyMovedMsgCount.addAndGet(1);
                } else if (msg instanceof NextTurnMessage) {
                    assertEquals(l.getPlayerToAct().getUid(), ((NextTurnMessage) msg).getPlayerToActID());
                    nextTurnMsgCount.addAndGet(1);
                }
            });
        }
        turnOrder.get(0).sendMessage(new ArmyMovedMessage(lobbyID,
                playerToAct.getUid(), fromTerritory.getId(),
                toTerritory.getId(), armyCountMoved));

        Thread.sleep(2000);
        // check that all messages were received by all clients
        assertEquals(NUM_CLIENTS, armyMovedMsgCount.get());
        assertEquals(NUM_CLIENTS, nextTurnMsgCount.get());
    }

    /**
     * Test that only the player to act can move armies.
     * @throws InterruptedException
     */
    @Test
    public void testMoveArmiesWrongTurn() throws InterruptedException {
        Lobby l = server.getDataStore().getLobbyByID(lobbyID);
        Player playerToAct = clientPlayers.get(turnOrder.get(1));
        List<Territory> territoriesOfPlayer = playerOccupiedTerritories.get(playerToAct.getUid());
        Territory fromTerritory = territoriesOfPlayer.get(0);
        Territory toTerritory = territoriesOfPlayer.get(1);

        for (NetworkClientKryo client : clients) {
            client.registerCallback(msg -> {
                if (msg instanceof ArmyMovedMessage) {
                    armyMovedMsgCount.addAndGet(1);
                } else if (msg instanceof NextTurnMessage) {
                    assertEquals(l.getPlayerToAct().getUid(), ((NextTurnMessage) msg).getPlayerToActID());
                    nextTurnMsgCount.addAndGet(1);
                }
            });
        }
        turnOrder.get(1).sendMessage(new ArmyMovedMessage(lobbyID,
                playerToAct.getUid(), fromTerritory.getId(),
                toTerritory.getId(), 1));

        Thread.sleep(2000);
        // check that all messages were received by all clients
        assertEquals(0, armyMovedMsgCount.get());
        assertEquals(0, nextTurnMsgCount.get());
    }

    /**
     * Test that at least one army has to remain on the territory moved from.
     * @throws InterruptedException
     */
    @Test
    public void testMoveArmiesCountTooHigh() throws InterruptedException {
        Lobby l = server.getDataStore().getLobbyByID(lobbyID);
        Player playerToAct = clientPlayers.get(turnOrder.get(0));
        List<Territory> territoriesOfPlayer = playerOccupiedTerritories.get(playerToAct.getUid());
        Territory fromTerritory = territoriesOfPlayer.get(0);
        Territory toTerritory = territoriesOfPlayer.get(1);

        for (NetworkClientKryo client : clients) {
            client.registerCallback(msg -> {
                if (msg instanceof ArmyMovedMessage) {
                    armyMovedMsgCount.addAndGet(1);
                } else if (msg instanceof NextTurnMessage) {
                    assertEquals(l.getPlayerToAct().getUid(), ((NextTurnMessage) msg).getPlayerToActID());
                    nextTurnMsgCount.addAndGet(1);
                }
            });
        }
        turnOrder.get(0).sendMessage(new ArmyMovedMessage(lobbyID,
                playerToAct.getUid(), fromTerritory.getId(),
                toTerritory.getId(), fromTerritory.getArmyCount()));

        Thread.sleep(2000);
        // check that all messages were received by all clients
        assertEquals(0, armyMovedMsgCount.get());
        assertEquals(0, nextTurnMsgCount.get());
    }

    @After
    public void tearDown() {
        disconnectAll();
    }
}
