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
import edu.aau.se2.server.networking.dto.game.AttackResultMessage;
import edu.aau.se2.server.networking.dto.game.LeftGameMessage;
import edu.aau.se2.server.networking.dto.game.NextTurnMessage;
import edu.aau.se2.server.networking.dto.game.VictoryMessage;
import edu.aau.se2.server.networking.dto.lobby.LeftLobbyMessage;
import edu.aau.se2.server.networking.dto.lobby.RequestLeaveLobby;
import edu.aau.se2.server.networking.kryonet.NetworkClientKryo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Tests regarding player leaving the game
 */
public class PlayerLeavesTest extends AbstractServerTest {
    private static final int NUM_CLIENTS = 3;

    private AtomicInteger nextTurnMsgCount;
    private AtomicInteger victoryMsgCount;
    private AtomicInteger leftGameMsgCount;
    private AtomicInteger attackResultMsgCount;
    private AtomicInteger leftLobbyMsgCount;

    private int lobbyID;
    private Map<NetworkClientKryo, Player> clientPlayers;
    private List<NetworkClientKryo> turnOrder;

    public PlayerLeavesTest() {
        super(NUM_CLIENTS);
    }

    @Before
    public void setup() throws IOException, TimeoutException {
        nextTurnMsgCount = new AtomicInteger(0);
        victoryMsgCount = new AtomicInteger(0);
        leftGameMsgCount = new AtomicInteger(0);
        attackResultMsgCount = new AtomicInteger(0);
        leftLobbyMsgCount = new AtomicInteger(0);
        // setup game until initial armies are placed
        server.start();
        clientPlayers = server.connect(Arrays.asList(clients), 5000);
        lobbyID = server.setupLobby(Arrays.asList(clients));
        turnOrder = server.startGame(lobbyID, clientPlayers);
        //server.placeInitialArmies(lobbyID);
    }

    /**
     * Test that if a player leaves the game during initial phase of game the game is closed
     * for everyone and the lobby is removed from the server
     *
     * @throws InterruptedException
     */
    @Test
    public void testPlayerLeavesDuringInitialArmyPlacement() throws InterruptedException {
        registerCallBacks(false);

        assertEquals(1, server.getDataStore().getLobbies().size());
        clients[0].sendMessage(new RequestLeaveLobby(lobbyID, clientPlayers.get(turnOrder.get(0)).getUid()));
        Thread.sleep(2000);

        // there should only be leftLobbyMessages sent
        assertEquals(0, nextTurnMsgCount.get());
        assertEquals(0, victoryMsgCount.get());
        assertEquals(0, leftGameMsgCount.get());
        assertEquals(3, leftLobbyMsgCount.get());
        assertEquals(0, attackResultMsgCount.get());

        // make sure lobby is closed after player left
        assertEquals(0, server.getDataStore().getLobbies().size());
    }

    /**
     * Test that if a player disconnects during initial phase of game the game is closed
     * for everyone and the lobby is removed from the server
     *
     * @throws InterruptedException
     */
    @Test
    public void testPlayerLeavesDuringInitialArmyPlacementDisconnect() throws InterruptedException {
        Lobby l = server.getDataStore().getLobbyByID(lobbyID);
        registerCallBacks(false);

        assertEquals(1, server.getDataStore().getLobbies().size());
        clients[0].disconnect();
        Thread.sleep(2000);

        // there should only be leftLobbyMessages sent
        assertEquals(0, nextTurnMsgCount.get());
        assertEquals(0, victoryMsgCount.get());
        assertEquals(0, leftGameMsgCount.get());
        assertEquals(2, leftLobbyMsgCount.get());
        assertEquals(0, attackResultMsgCount.get());

        // make sure lobby is closed after player left
        assertEquals(0, server.getDataStore().getLobbies().size());
    }

    /**
     * Test that player can leave the game without problems if he has already lost and is only
     * spectating
     *
     * @throws InterruptedException
     */
    @Test
    public void testPlayerWhoLostLeaves() throws InterruptedException {
        server.placeInitialArmies(lobbyID);
        registerCallBacks(true);

        Lobby l = server.getDataStore().getLobbyByID(lobbyID);
        Player loser = l.getPlayerByID(l.getTurnOrder().get(0));
        loser.setHasLost(true);

        assertEquals(1, server.getDataStore().getLobbies().size());
        clients[0].sendMessage(new RequestLeaveLobby(lobbyID, clientPlayers.get(turnOrder.get(0)).getUid()));
        Thread.sleep(2000);

        // there should only be leftGameMessages sent
        assertEquals(0, nextTurnMsgCount.get());
        assertEquals(0, victoryMsgCount.get());
        assertEquals(3, leftGameMsgCount.get());
        assertEquals(0, leftLobbyMsgCount.get());
        assertEquals(0, attackResultMsgCount.get());

        // make sure lobby is still open after player left
        assertEquals(1, server.getDataStore().getLobbies().size());
    }

    /**
     * Test if a player who already won can leave without problems
     *
     * @throws InterruptedException
     */
    @Test
    public void testPlayerWhoWonLeaves() throws InterruptedException {
        server.placeInitialArmies(lobbyID);
        registerCallBacks(true);

        Lobby l = server.getDataStore().getLobbyByID(lobbyID);
        l.getTurnOrder().remove(1);
        l.getTurnOrder().remove(1);

        assertEquals(1, server.getDataStore().getLobbies().size());
        clients[0].sendMessage(new RequestLeaveLobby(lobbyID, clientPlayers.get(turnOrder.get(0)).getUid()));
        Thread.sleep(2000);

        // there should only be leftGameMessages sent
        assertEquals(0, nextTurnMsgCount.get());
        assertEquals(0, victoryMsgCount.get());
        assertEquals(3, leftGameMsgCount.get());
        assertEquals(0, leftLobbyMsgCount.get());
        assertEquals(0, attackResultMsgCount.get());

        // make sure lobby is still open after player left
        assertEquals(1, server.getDataStore().getLobbies().size());
    }

    /**
     * Test if an active attack is ended, if one of the involved parties disconnects or leaves on
     * purpose, make also sure the next turn is started properly
     *
     * @throws InterruptedException
     */
    @Test
    public void testPlayerLeaveDuringAttackInvolved() throws InterruptedException {
        server.placeInitialArmies(lobbyID);
        Lobby l = server.getDataStore().getLobbyByID(lobbyID);
        Player attacker = clientPlayers.get(turnOrder.get(0));
        Territory attackerTerritory = l.getTerritoriesOccupiedByPlayer(attacker.getUid())[0];
        Territory defenderTerritory = l.getTerritoriesOccupiedByPlayer(clientPlayers.get(turnOrder.get(1)).getUid())[0];
        registerCallBacks(false);
        server.setupPreOccupy(lobbyID, attackerTerritory.getId(), defenderTerritory.getId());
        server.getDataStore().updateLobby(l);


        assertEquals(1, server.getDataStore().getLobbies().size());
        // player disconnects during attack
        clients[clientPlayers.get(turnOrder.get(0)).getUid() - 1].disconnect();
        Thread.sleep(2000);

        // disconnect during attack, only other players will get messages
        assertEquals(2, nextTurnMsgCount.get());
        assertEquals(0, victoryMsgCount.get());
        assertEquals(2, leftGameMsgCount.get());
        assertEquals(0, leftLobbyMsgCount.get());
        assertEquals(2, attackResultMsgCount.get());

        // make sure lobby is still open after player left
        assertEquals(1, server.getDataStore().getLobbies().size());
        // make sure attack is over
        assertNull(l.getCurrentAttack());
        // make sure players territories are set unoccupied
        assertEquals(0, l.getTerritoriesOccupiedByPlayer(attacker.getUid()).length);
    }

    /**
     * Test that players who are not involved in an active attack can leave without ending the
     * attack or the whole game
     *
     * @throws InterruptedException
     */
    @Test
    public void testPlayerLeaveDuringAttackNotInvolved() throws InterruptedException {
        server.placeInitialArmies(lobbyID);
        Lobby l = server.getDataStore().getLobbyByID(lobbyID);
        Player attacker = clientPlayers.get(turnOrder.get(0));
        Territory attackerTerritory = l.getTerritoriesOccupiedByPlayer(attacker.getUid())[0];
        Territory defenderTerritory = l.getTerritoriesOccupiedByPlayer(clientPlayers.get(turnOrder.get(1)).getUid())[0];
        registerCallBacks(false);
        server.setupPreOccupy(lobbyID, attackerTerritory.getId(), defenderTerritory.getId());
        server.getDataStore().updateLobby(l);


        assertEquals(1, server.getDataStore().getLobbies().size());
        clients[2].sendMessage(new RequestLeaveLobby(lobbyID, clientPlayers.get(turnOrder.get(2)).getUid()));
        Thread.sleep(2000);

        // everyone will receive the message that a player left
        assertEquals(0, nextTurnMsgCount.get());
        assertEquals(0, victoryMsgCount.get());
        assertEquals(3, leftGameMsgCount.get());
        assertEquals(0, leftLobbyMsgCount.get());
        assertEquals(0, attackResultMsgCount.get());

        // make sure lobby is still open after player left
        assertEquals(1, server.getDataStore().getLobbies().size());
        //make sure attack is not ended
        assertNotNull(l.getCurrentAttack());
        // make sure players territories are set unoccupied
        assertEquals(0, l.getTerritoriesOccupiedByPlayer(clientPlayers.get(turnOrder.get(2)).getUid()).length);
    }

    /**
     * Test that a player is winning if everyone else left or disconnected
     *
     * @throws InterruptedException
     */
    @Test
    public void testLastPlayerLeftWins() throws InterruptedException {
        server.placeInitialArmies(lobbyID);
        registerCallBacks(false);

        Lobby l = server.getDataStore().getLobbyByID(lobbyID);
        l.getTurnOrder().remove(2);

        assertEquals(1, server.getDataStore().getLobbies().size());
        clients[1].sendMessage(new RequestLeaveLobby(lobbyID, clientPlayers.get(turnOrder.get(1)).getUid()));
        Thread.sleep(2000);

        assertEquals(0, nextTurnMsgCount.get());
        assertEquals(2, victoryMsgCount.get());
        assertEquals(3, leftGameMsgCount.get());
        assertEquals(0, leftLobbyMsgCount.get());
        assertEquals(0, attackResultMsgCount.get());

        // make sure lobby is still open after player won
        assertEquals(1, server.getDataStore().getLobbies().size());
    }

    /**
     * Test that the turn is ended properly, if its the leaving players turn
     *
     * @throws InterruptedException
     */
    @Test
    public void testPlayersTurnEndedOnLeave() throws InterruptedException {
        server.placeInitialArmies(lobbyID);
        registerCallBacks(false);

        assertEquals(1, server.getDataStore().getLobbies().size());
        clients[0].sendMessage(new RequestLeaveLobby(lobbyID, clientPlayers.get(turnOrder.get(0)).getUid()));
        Thread.sleep(2000);

        assertEquals(2, nextTurnMsgCount.get());
        assertEquals(0, victoryMsgCount.get());
        assertEquals(3, leftGameMsgCount.get());
        assertEquals(0, leftLobbyMsgCount.get());
        assertEquals(0, attackResultMsgCount.get());

        // make sure lobby is still open after player won
        assertEquals(1, server.getDataStore().getLobbies().size());
    }

    /**
     * Make sure that a lobby is removed from server if all players left the game
     *
     * @throws InterruptedException
     */
    @Test
    public void testLastPlayerLeftRemoveLobby() throws InterruptedException {
        server.placeInitialArmies(lobbyID);
        registerCallBacks(false);

        assertEquals(1, server.getDataStore().getLobbies().size());
        clients[0].sendMessage(new RequestLeaveLobby(lobbyID, clientPlayers.get(turnOrder.get(0)).getUid()));
        clients[1].sendMessage(new RequestLeaveLobby(lobbyID, clientPlayers.get(turnOrder.get(1)).getUid()));
        clients[2].sendMessage(new RequestLeaveLobby(lobbyID, clientPlayers.get(turnOrder.get(2)).getUid()));
        Thread.sleep(2000);

        assertEquals(1, victoryMsgCount.get());
        assertEquals(5, leftGameMsgCount.get());
        assertEquals(0, leftLobbyMsgCount.get());
        assertEquals(0, attackResultMsgCount.get());

        // make sure lobby is still open after player won
        assertEquals(0, server.getDataStore().getLobbies().size());
    }

    /**
     * Helper method for registering all possible callbacks
     */
    private void registerCallBacks(boolean lostGame) {
        for (NetworkClientKryo client : clients) {
            client.registerCallback(msg -> {
                if (msg instanceof LeftLobbyMessage) {
                    if (((LeftLobbyMessage) msg).isWasClosed()) leftLobbyMsgCount.addAndGet(1);
                } else if (msg instanceof LeftGameMessage) {
                    if (((LeftGameMessage) msg).isHasLost() == lostGame)
                        leftGameMsgCount.addAndGet(1);
                } else if (msg instanceof VictoryMessage) {
                    victoryMsgCount.addAndGet(1);
                } else if (msg instanceof NextTurnMessage) {
                    nextTurnMsgCount.addAndGet(1);
                } else if (msg instanceof AttackResultMessage) {
                    attackResultMsgCount.addAndGet(1);
                }
            });
        }
    }

    @After
    public void tearDown() {
        disconnectAll();
    }
}
