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
import java.util.concurrent.atomic.AtomicReference;

import edu.aau.se2.server.data.CardDeck;
import edu.aau.se2.server.data.Lobby;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.networking.dto.game.NewCardMessage;
import edu.aau.se2.server.networking.dto.game.NextTurnMessage;
import edu.aau.se2.server.networking.kryonet.NetworkClientKryo;

import static org.junit.Assert.assertEquals;

/**
 * Tests regarding NewCardMessages
 */
public class NewCardTest extends AbstractServerTest {
    private static final int NUM_CLIENTS = 3;

    private AtomicInteger newCardMsgCount;
    private AtomicInteger nextTurnMsgCount;

    private int lobbyID;
    private Map<NetworkClientKryo, Player> clientPlayers;
    private List<NetworkClientKryo> turnOrder;

    public NewCardTest() {
        super(NUM_CLIENTS);
    }

    @Before
    public void setup() throws IOException, TimeoutException {
        newCardMsgCount = new AtomicInteger(0);
        nextTurnMsgCount = new AtomicInteger(0);
        // setup game until initial armies are placed
        server.start();
        clientPlayers = server.connect(Arrays.asList(clients), 5000);
        lobbyID = server.setupLobby(Arrays.asList(clients));
        turnOrder = server.startGame(lobbyID, clientPlayers);
        server.placeInitialArmies(lobbyID);
        server.setTurnArmiesPlaced(lobbyID);
    }

    /**
     * Sends NextTurnMessage to server and expects to receive a newCardMessage in return
     * newCardMessage should only be sent to the user that ended his turn
     * Also checks if the cardName received from server is the same as the one that is assigned
     * to the player server-sided
     *
     * @throws InterruptedException
     */
    @Test
    public void testNewCardMessage() throws InterruptedException {
        Lobby l = server.getDataStore().getLobbyByID(lobbyID);
        Player playerToAct = clientPlayers.get(turnOrder.get(0));
        AtomicReference<String> cardName = new AtomicReference<>("");

        for (NetworkClientKryo client : clients) {
            client.registerCallback(msg -> {
                if (msg instanceof NextTurnMessage) {
                    assertEquals(l.getPlayerToAct().getUid(), ((NextTurnMessage) msg).getPlayerToActID());
                    nextTurnMsgCount.addAndGet(1);
                } else if (msg instanceof NewCardMessage) {
                    assertEquals(playerToAct.getUid(), ((NewCardMessage) msg).getFromPlayerID());
                    assertEquals('c', ((NewCardMessage) msg).getCardName().charAt(0));
                    assertEquals('_', ((NewCardMessage) msg).getCardName().charAt(4));
                    cardName.set(((NewCardMessage) msg).getCardName());
                    newCardMsgCount.addAndGet(1);
                }
            });
        }
        turnOrder.get(0).sendMessage(new NextTurnMessage(lobbyID, playerToAct.getUid()));
        Thread.sleep(2000);
        // check that all users got nextTurnMessage, and only 1 user got newCardMessage
        assertEquals(cardName.toString(), l.getCardDeck().getCardNamesOfPlayer(playerToAct.getUid())[0]);
        assertEquals(1, newCardMsgCount.get());
        assertEquals(NUM_CLIENTS, nextTurnMsgCount.get());
    }

    /**
     * Should only receive a new card when ending turn and being playerToAct
     *
     * @throws InterruptedException
     */
    @Test
    public void testNewCardMessageWrongTurn() throws InterruptedException {
        Lobby l = server.getDataStore().getLobbyByID(lobbyID);
        Player playerToAct = clientPlayers.get(turnOrder.get(1));

        for (NetworkClientKryo client : clients) {
            client.registerCallback(msg -> {
                if (msg instanceof NextTurnMessage) {
                    assertEquals(l.getPlayerToAct().getUid(), ((NextTurnMessage) msg).getPlayerToActID());
                    nextTurnMsgCount.addAndGet(1);
                } else if (msg instanceof NewCardMessage) {
                    assertEquals(playerToAct.getUid(), ((NewCardMessage) msg).getFromPlayerID());
                    newCardMsgCount.addAndGet(1);
                }
            });
        }
        turnOrder.get(1).sendMessage(new NextTurnMessage(lobbyID, playerToAct.getUid()));
        Thread.sleep(2000);
        // check that no one got nextTurnMessage and newCardMessage
        assertEquals(0, newCardMsgCount.get());
        assertEquals(0, nextTurnMsgCount.get());
    }

    /**
     * Player only receives a newCardMessage if there are Cards left in the deck
     *
     * @throws InterruptedException
     */
    @Test
    public void testNewCardMessageNoCardsLeft() throws InterruptedException {
        Lobby l = server.getDataStore().getLobbyByID(lobbyID);
        Player playerToAct = clientPlayers.get(turnOrder.get(0));
        CardDeck cardDeck = l.getCardDeck();
        for (int i = 0; i < cardDeck.getDeck().length; i++) {
            l.getCardDeck().getRandomCard(0);
        }

        for (NetworkClientKryo client : clients) {
            client.registerCallback(msg -> {
                if (msg instanceof NextTurnMessage) {
                    assertEquals(l.getPlayerToAct().getUid(), ((NextTurnMessage) msg).getPlayerToActID());
                    nextTurnMsgCount.addAndGet(1);
                } else if (msg instanceof NewCardMessage) {
                    assertEquals(playerToAct.getUid(), ((NewCardMessage) msg).getFromPlayerID());
                    newCardMsgCount.addAndGet(1);
                }
            });
        }
        turnOrder.get(0).sendMessage(new NextTurnMessage(lobbyID, playerToAct.getUid()));
        Thread.sleep(2000);
        // check that all users got nextTurnMessage, and 0 users got newCardMessage since no card is left
        assertEquals(0, newCardMsgCount.get());
        assertEquals(NUM_CLIENTS, nextTurnMsgCount.get());
    }

    @After
    public void tearDown() {
        disconnectAll();
    }
}
