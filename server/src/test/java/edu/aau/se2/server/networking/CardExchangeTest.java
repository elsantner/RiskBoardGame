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

import edu.aau.se2.server.data.CardDeck;
import edu.aau.se2.server.data.Lobby;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.logic.ArmyCountHelper;
import edu.aau.se2.server.networking.dto.game.CardExchangeMessage;
import edu.aau.se2.server.networking.dto.game.NewArmiesMessage;
import edu.aau.se2.server.networking.dto.game.RefreshCardsMessage;
import edu.aau.se2.server.networking.kryonet.NetworkClientKryo;

import static org.junit.Assert.assertEquals;

/**
 * Tests regarding CardExchangeMessages, NewArmiesMessage, RefreshCardsMessage
 */
public class CardExchangeTest extends AbstractServerTest {
    private static final int NUM_CLIENTS = 3;

    private AtomicInteger nextArmiesMsgCount;
    private AtomicInteger nextRefreshCardsMsgCount;

    private int lobbyID;
    private Map<NetworkClientKryo, Player> clientPlayers;
    private List<NetworkClientKryo> turnOrder;

    public CardExchangeTest() {
        super(NUM_CLIENTS);
    }

    @Before
    public void setup() throws IOException, TimeoutException {
        nextArmiesMsgCount = new AtomicInteger(0);
        nextRefreshCardsMsgCount = new AtomicInteger(0);
        // setup game until initial armies are placed
        server.start();
        clientPlayers = server.connect(Arrays.asList(clients), 5000);
        lobbyID = server.setupLobby(Arrays.asList(clients));
        turnOrder = server.startGame(lobbyID, clientPlayers);
        server.placeInitialArmies(lobbyID);
        server.setTurnArmiesPlaced(lobbyID);
    }

    /**
     * Tests if cardExchangeMessage received by server is handled properly, also tests the armyCount
     * that is returned by the server, to all clients.
     * This is the Testcase, where no card exchange should occur.
     *
     * @throws InterruptedException
     */
    @Test
    public void testCardExchangeMessageNoExchange() throws InterruptedException {
        Lobby l = server.getDataStore().getLobbyByID(lobbyID);
        Player playerToAct = clientPlayers.get(turnOrder.get(0));

        for (NetworkClientKryo client : clients) {
            client.registerCallback(msg -> {
                if (msg instanceof NewArmiesMessage) {
                    assertEquals(l.getPlayerToAct().getUid(), ((NewArmiesMessage) msg).getFromPlayerID());
                    assertEquals(playerToAct.getUid(), ((NewArmiesMessage) msg).getFromPlayerID());
                    assertEquals(ArmyCountHelper.getNewArmyCount(l.getTerritoriesOccupiedByPlayer(playerToAct.getUid()),
                            playerToAct.getUid()), ((NewArmiesMessage) msg).getNewArmyCount());
                    nextArmiesMsgCount.addAndGet(1);
                } else if (msg instanceof RefreshCardsMessage) {
                    assertEquals(l.getPlayerToAct().getUid(), ((RefreshCardsMessage) msg).getFromPlayerID());
                    assertEquals(playerToAct.getUid(), ((RefreshCardsMessage) msg).getFromPlayerID());
                    nextRefreshCardsMsgCount.addAndGet(1);
                }
            });
        }
        turnOrder.get(0).sendMessage(new CardExchangeMessage(lobbyID, playerToAct.getUid(), false));
        Thread.sleep(2000);
        // check that all users got nextArmiesMsg, and none got Card
        assertEquals(NUM_CLIENTS, nextArmiesMsgCount.get());
        assertEquals(0, nextRefreshCardsMsgCount.get());
    }

    /**
     * Tests if cardExchangeMessage received by server is handled properly, also tests the armyCount
     * that is returned by the server, to all clients.
     * This is the Testcase, where a card exchange occurs.
     *
     * @throws InterruptedException
     */
    @Test
    public void testCardExchangeMessageWithExchange() throws InterruptedException {
        Lobby l = server.getDataStore().getLobbyByID(lobbyID);
        Player playerToAct = clientPlayers.get(turnOrder.get(0)); //this is not the same player as the one in lobby!!
        CardDeck cardDeck = l.getCardDeck();
        Player p = l.getPlayerToAct();
        for (int i = 0; i < cardDeck.getDeck().length; i++) {
            l.getCardDeck().getRandomCard(p.getUid());
        }
        p.setTradableSet(l.getCardDeck().getCardSet(p.getUid()));

        for (NetworkClientKryo client : clients) {
            client.registerCallback(msg -> {
                if (msg instanceof NewArmiesMessage) {
                    assertEquals(l.getPlayerToAct().getUid(), ((NewArmiesMessage) msg).getFromPlayerID());
                    assertEquals(playerToAct.getUid(), ((NewArmiesMessage) msg).getFromPlayerID());
                    int armyCount = ArmyCountHelper.getNewArmyCount(l.getTerritoriesOccupiedByPlayer(playerToAct.getUid()), playerToAct.getUid());
                    armyCount += 4;
                    assertEquals(armyCount, ((NewArmiesMessage) msg).getNewArmyCount());
                    nextArmiesMsgCount.addAndGet(1);
                } else if (msg instanceof RefreshCardsMessage) {
                    assertEquals(l.getPlayerToAct().getUid(), ((RefreshCardsMessage) msg).getFromPlayerID());
                    assertEquals(playerToAct.getUid(), ((RefreshCardsMessage) msg).getFromPlayerID());
                    nextRefreshCardsMsgCount.addAndGet(1);
                }
            });
        }
        turnOrder.get(0).sendMessage(new CardExchangeMessage(l.getLobbyID(), playerToAct.getUid(), true));
        Thread.sleep(2000);
        // check that all users got nextArmiesMsg, and 1 got RefreshCardsMessage, the number of SetsTradedIn should be 1
        assertEquals(1, l.getCardDeck().getSetsTradedIn());
        assertEquals(NUM_CLIENTS, nextArmiesMsgCount.get());
        assertEquals(1, nextRefreshCardsMsgCount.get());
    }

    /**
     * If its not the turn of this player nothing should happen on receiving CardExchangeMessage
     *
     * @throws InterruptedException
     */
    @Test
    public void testCardExchangeMessageWrongTurn() throws InterruptedException {
        Lobby l = server.getDataStore().getLobbyByID(lobbyID);
        Player playerToAct = clientPlayers.get(turnOrder.get(1));

        for (NetworkClientKryo client : clients) {
            client.registerCallback(msg -> {
                if (msg instanceof NewArmiesMessage) {
                    assertEquals(l.getPlayerToAct().getUid(), ((NewArmiesMessage) msg).getFromPlayerID());
                    assertEquals(playerToAct.getUid(), ((NewArmiesMessage) msg).getFromPlayerID());
                    assertEquals(ArmyCountHelper.getNewArmyCount(l.getTerritoriesOccupiedByPlayer(playerToAct.getUid()),
                            playerToAct.getUid()), ((NewArmiesMessage) msg).getNewArmyCount());
                    nextArmiesMsgCount.addAndGet(1);
                } else if (msg instanceof RefreshCardsMessage) {
                    assertEquals(l.getPlayerToAct().getUid(), ((RefreshCardsMessage) msg).getFromPlayerID());
                    assertEquals(playerToAct.getUid(), ((RefreshCardsMessage) msg).getFromPlayerID());
                    nextRefreshCardsMsgCount.addAndGet(1);
                }
            });
        }
        turnOrder.get(0).sendMessage(new CardExchangeMessage(lobbyID, playerToAct.getUid(), false));
        Thread.sleep(2000);
        // check that all users got nextArmiesMsg, and none got Card
        assertEquals(0, nextArmiesMsgCount.get());
        assertEquals(0, nextRefreshCardsMsgCount.get());
    }

    @After
    public void tearDown() {
        disconnectAll();
    }
}
