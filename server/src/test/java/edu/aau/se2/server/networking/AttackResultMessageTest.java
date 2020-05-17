package edu.aau.se2.server.networking;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import edu.aau.se2.server.data.Attack;
import edu.aau.se2.server.data.Lobby;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.data.Territory;
import edu.aau.se2.server.networking.dto.game.AttackResultMessage;
import edu.aau.se2.server.networking.dto.game.AttackStartedMessage;
import edu.aau.se2.server.networking.dto.game.DefenderDiceCountMessage;
import edu.aau.se2.server.networking.dto.game.DiceResultMessage;
import edu.aau.se2.server.networking.kryonet.NetworkClientKryo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AttackResultMessageTest extends AbstractServerTest {
    private static final int NUM_CLIENTS = 3;

    private AtomicInteger countAttackResultMsgsReceived1 = new AtomicInteger(0);
    private AtomicInteger countAttackResultMsgsReceived2 = new AtomicInteger(0);
    private AtomicInteger countAttackResultMsgsReceived3 = new AtomicInteger(0);

    private Map<NetworkClientKryo, Player> clientPlayers;
    private int lobbyID;
    private List<NetworkClientKryo> turnOrder ;

    private Map<Integer, List<Territory>> playerOccupiedTerritories;

    public AttackResultMessageTest() {
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

        Lobby l = server.getDataStore().getLobbyByID(lobbyID);
        assertTrue(l.attackRunning());
        List<Integer> results = new ArrayList<>();
        results.add(2);
        clients[0].sendMessage(new DiceResultMessage(lobbyID, attacker.getUid(), results, true));
        clients[1].sendMessage(new DefenderDiceCountMessage(lobbyID, defender.getUid(), 1));

        Thread.sleep(1500);
    }

    @Test
    public void testAttackResultWinningDefender() throws InterruptedException {

        Player defender = clientPlayers.get(turnOrder.get(1));
        for (NetworkClientKryo client : clients) {
            client.registerCallback(msg -> {
                if (msg instanceof AttackResultMessage) {
                    countAttackResultMsgsReceived1.addAndGet(1);
                }
            });
        }

        Lobby l = server.getDataStore().getLobbyByID(lobbyID);
        int fromTerritoryID = l.getCurrentAttack().getFromTerritoryID();
        int toTerritoryID = l.getCurrentAttack().getToTerritoryID();
        assertTrue(l.attackRunning());
        assertEquals(3, l.getTerritoryByID(fromTerritoryID).getArmyCount());
        assertEquals(3, l.getTerritoryByID(toTerritoryID).getArmyCount());
        List<Integer> results = new ArrayList<>();
        results.add(3);
        clients[1].sendMessage(new DiceResultMessage(lobbyID, defender.getUid(), results,false));

        Thread.sleep(6000);

        Attack attack = server.getDataStore().getLobbyByID(lobbyID).getCurrentAttack();
        assertNull(attack);
        assertEquals(2, l.getTerritoryByID(fromTerritoryID).getArmyCount());
        assertEquals(3, l.getTerritoryByID(toTerritoryID).getArmyCount());
        assertEquals(NUM_CLIENTS, countAttackResultMsgsReceived1.get());
    }

    @Test
    public void testAttackResultWinningAttacker() throws InterruptedException {

        Player defender = clientPlayers.get(turnOrder.get(1));
        for (NetworkClientKryo client : clients) {
            client.registerCallback(msg -> {
                if (msg instanceof AttackResultMessage) {
                    countAttackResultMsgsReceived2.addAndGet(1);
                }
            });
        }

        Lobby l = server.getDataStore().getLobbyByID(lobbyID);
        int fromTerritoryID = l.getCurrentAttack().getFromTerritoryID();
        int toTerritoryID = l.getCurrentAttack().getToTerritoryID();
        assertTrue(l.attackRunning());
        assertEquals(3, l.getTerritoryByID(fromTerritoryID).getArmyCount());
        assertEquals(3, l.getTerritoryByID(toTerritoryID).getArmyCount());
        List<Integer> results = new ArrayList<>();
        results.add(1);
        clients[1].sendMessage(new DiceResultMessage(lobbyID, defender.getUid(), results,false));

        Thread.sleep(6000);

        Attack attack = server.getDataStore().getLobbyByID(lobbyID).getCurrentAttack();
        assertNull(attack);
        assertEquals(3, l.getTerritoryByID(fromTerritoryID).getArmyCount());
        assertEquals(2, l.getTerritoryByID(toTerritoryID).getArmyCount());
        assertEquals(NUM_CLIENTS, countAttackResultMsgsReceived2.get());
    }

    @Test
    public void testAttackResultDraw() throws InterruptedException {

        Player defender = clientPlayers.get(turnOrder.get(1));
        for (NetworkClientKryo client : clients) {
            client.registerCallback(msg -> {
                if (msg instanceof AttackResultMessage) {
                    countAttackResultMsgsReceived3.addAndGet(1);
                }
            });
        }

        Lobby l = server.getDataStore().getLobbyByID(lobbyID);
        int fromTerritoryID = l.getCurrentAttack().getFromTerritoryID();
        int toTerritoryID = l.getCurrentAttack().getToTerritoryID();
        assertTrue(l.attackRunning());
        assertEquals(3, l.getTerritoryByID(fromTerritoryID).getArmyCount());
        assertEquals(3, l.getTerritoryByID(toTerritoryID).getArmyCount());
        List<Integer> results = new ArrayList<>();
        results.add(2);
        clients[1].sendMessage(new DiceResultMessage(lobbyID, defender.getUid(), results,false));

        Thread.sleep(6000);

        Attack attack = server.getDataStore().getLobbyByID(lobbyID).getCurrentAttack();
        assertNull(attack);
        assertEquals(3, l.getTerritoryByID(fromTerritoryID).getArmyCount());
        assertEquals(2, l.getTerritoryByID(toTerritoryID).getArmyCount());
        assertEquals(NUM_CLIENTS, countAttackResultMsgsReceived3.get());
    }

    @After
    public void tearDown() {
        disconnectAll();
    }
}
