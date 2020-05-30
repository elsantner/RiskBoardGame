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
import edu.aau.se2.server.networking.dto.game.OccupyTerritoryMessage;
import edu.aau.se2.server.networking.dto.game.PlayerLostMessage;
import edu.aau.se2.server.networking.dto.game.VictoryMessage;
import edu.aau.se2.server.networking.kryonet.NetworkClientKryo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests regarding Victory- and PlayerLostMessage
 */
public class VictoryAndLoseTest extends AbstractServerTest {
    private static final int NUM_CLIENTS = 3;

    private AtomicInteger playerLostMsgCount;
    private AtomicInteger victoryMsgCount;
    private AtomicInteger occupyTerritoryMsgCount;

    private int lobbyID;
    private Map<NetworkClientKryo, Player> clientPlayers;
    private List<NetworkClientKryo> turnOrder;

    public VictoryAndLoseTest() {
        super(NUM_CLIENTS);
    }

    @Before
    public void setup() throws IOException, TimeoutException {
        playerLostMsgCount = new AtomicInteger(0);
        victoryMsgCount = new AtomicInteger(0);
        occupyTerritoryMsgCount = new AtomicInteger(0);
        // setup game until initial armies are placed
        server.start();
        clientPlayers = server.connect(Arrays.asList(clients), 5000);
        lobbyID = server.setupLobby(Arrays.asList(clients));
        turnOrder = server.startGame(lobbyID, clientPlayers);
        server.placeInitialArmies(lobbyID);
    }

    /**
     * Test if a victory is broadcast after a player has won every Territory
     * for this to occur player needs to send successful occupyTerritoryMessage
     * player who loses needs to only have one last territory left
     *
     * @throws InterruptedException
     */
    @Test
    public void testPlayerVictory() throws InterruptedException {
        Lobby l = server.getDataStore().getLobbyByID(lobbyID);
        Player attacker = clientPlayers.get(turnOrder.get(0));
        Player loser = l.getPlayerByID(l.getTurnOrder().get(1));

        Territory attackerTerritory = l.getTerritoriesOccupiedByPlayer(attacker.getUid())[0];
        Territory defenderTerritory = l.getTerritoriesOccupiedByPlayer(clientPlayers.get(turnOrder.get(1)).getUid())[0];

        // make sure loser only has a single territory left
        for (int i = 0; i < l.getNumberOfTerritories(); i++) {
            if (l.getTerritoryByID(i) != attackerTerritory && l.getTerritoryByID(i) != defenderTerritory) {
                l.getTerritoryByID(i).setOccupierPlayerID(-1);
            }
        }

        for (NetworkClientKryo client : clients) {
            client.registerCallback(msg -> {
                if (msg instanceof OccupyTerritoryMessage) {
                    occupyTerritoryMsgCount.addAndGet(1);
                } else if (msg instanceof VictoryMessage) {
                    victoryMsgCount.addAndGet(1);
                } else if (msg instanceof PlayerLostMessage) {
                    playerLostMsgCount.addAndGet(1);
                }
            });
        }

        server.setupPreOccupy(lobbyID, attackerTerritory.getId(), defenderTerritory.getId());
        server.getDataStore().updateLobby(l);
        clients[0].sendMessage(new OccupyTerritoryMessage(lobbyID, attacker.getUid(), defenderTerritory.getId(), attackerTerritory.getId(),
                attackerTerritory.getArmyCount() - 1));
        Thread.sleep(2000);

        // there should be no playerLostMessages, since there is a winner and everyone else has lost
        assertEquals(0, playerLostMsgCount.get());
        assertEquals(3, victoryMsgCount.get());
        assertEquals(3, occupyTerritoryMsgCount.get());

        // make sure player is set to have lost
        assertTrue(loser.isHasLost());
    }


    /**
     * Make sure there is no victory or lose broadcast, if players still have territories left
     * after an occupation
     *
     * @throws InterruptedException
     */
    @Test
    public void testPlayerVictoryInvalid() throws InterruptedException {
        Lobby l = server.getDataStore().getLobbyByID(lobbyID);
        Player attacker = clientPlayers.get(turnOrder.get(0));

        // everyone has multiple territories, so no one can lose
        for (int i = 0; i < 42; i++) {
            l.getTerritoryByID(i).setOccupierPlayerID((i % 3) + 1);
        }

        Territory attackerTerritory = l.getTerritoriesOccupiedByPlayer(attacker.getUid())[0];
        Territory defenderTerritory = l.getTerritoriesOccupiedByPlayer(clientPlayers.get(turnOrder.get(1)).getUid())[0];

        for (NetworkClientKryo client : clients) {
            client.registerCallback(msg -> {
                if (msg instanceof OccupyTerritoryMessage) {
                    occupyTerritoryMsgCount.addAndGet(1);
                } else if (msg instanceof VictoryMessage) {
                    victoryMsgCount.addAndGet(1);
                } else if (msg instanceof PlayerLostMessage) {
                    playerLostMsgCount.addAndGet(1);
                }
            });
        }

        server.setupPreOccupy(lobbyID, attackerTerritory.getId(), defenderTerritory.getId());
        server.getDataStore().updateLobby(l);
        clients[0].sendMessage(new OccupyTerritoryMessage(lobbyID, attacker.getUid(), defenderTerritory.getId(), attackerTerritory.getId(),
                attackerTerritory.getArmyCount() - 1));
        Thread.sleep(2000);

        // Server should only sent occupyTerritoryMsg back, no other ones
        assertEquals(0, playerLostMsgCount.get());
        assertEquals(0, victoryMsgCount.get());
        assertEquals(3, occupyTerritoryMsgCount.get());
    }

    /**
     * Make sure a PlayerLostMessage is sent if a player has lost every territory
     * Every player in Lobby should receive it
     *
     * @throws InterruptedException
     */
    @Test
    public void testPlayerLose() throws InterruptedException {
        Lobby l = server.getDataStore().getLobbyByID(lobbyID);
        Player attacker = clientPlayers.get(turnOrder.get(0));
        Player loser = l.getPlayerByID(l.getTurnOrder().get(1));

        Territory attackerTerritory = l.getTerritoriesOccupiedByPlayer(attacker.getUid())[0];
        Territory defenderTerritory = l.getTerritoriesOccupiedByPlayer(clientPlayers.get(turnOrder.get(1)).getUid())[0];

        //remove all except 1 territory from the player to lose
        Territory[] territories = l.getTerritoriesOccupiedByPlayer(clientPlayers.get(turnOrder.get(1)).getUid());
        for (int i = 1; i < territories.length; i++) {
            territories[i].setOccupierPlayerID(-1);
        }

        for (NetworkClientKryo client : clients) {
            client.registerCallback(msg -> {
                if (msg instanceof OccupyTerritoryMessage) {
                    occupyTerritoryMsgCount.addAndGet(1);
                } else if (msg instanceof VictoryMessage) {
                    victoryMsgCount.addAndGet(1);
                } else if (msg instanceof PlayerLostMessage) {
                    playerLostMsgCount.addAndGet(1);
                }
            });
        }

        server.setupPreOccupy(lobbyID, attackerTerritory.getId(), defenderTerritory.getId());
        server.getDataStore().updateLobby(l);
        clients[0].sendMessage(new OccupyTerritoryMessage(lobbyID, attacker.getUid(), defenderTerritory.getId(), attackerTerritory.getId(),
                attackerTerritory.getArmyCount() - 1));
        Thread.sleep(2000);

        // Player loses, everyone should get occupyTerritoryMsg and playerLostMsg
        assertEquals(3, playerLostMsgCount.get());
        assertEquals(0, victoryMsgCount.get());
        assertEquals(3, occupyTerritoryMsgCount.get());

        // make sure player is set to have lost
        assertTrue(loser.isHasLost());
    }

    @After
    public void tearDown() {
        disconnectAll();
    }
}
