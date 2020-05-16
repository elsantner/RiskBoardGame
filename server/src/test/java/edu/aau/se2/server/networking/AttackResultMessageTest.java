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
import java.util.concurrent.atomic.AtomicInteger;

import edu.aau.se2.server.data.Attack;
import edu.aau.se2.server.data.Lobby;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.networking.dto.game.AttackResultMessage;
import edu.aau.se2.server.networking.dto.game.AttackStartedMessage;
import edu.aau.se2.server.networking.dto.game.DefenderDiceCountMessage;
import edu.aau.se2.server.networking.dto.game.DiceResultMessage;
import edu.aau.se2.server.networking.kryonet.NetworkClientKryo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AttackResultMessageTest {
    private static final int NUM_CLIENTS = 3;

    private AtomicInteger countAttackResultMsgsReceived1 = new AtomicInteger(0);
    private AtomicInteger countAttackResultMsgsReceived2 = new AtomicInteger(0);
    private AtomicInteger countAttackResultMsgsReceived3 = new AtomicInteger(0);

    private MainServerTestable server;
    private NetworkClientKryo[] clients;
    private int lobbyID;
    private List<Integer> turnOrder;
    private Map<Integer, Player> players;

    @Before
    public void setup() throws IOException, InterruptedException {
        // setup game until initial armies are placed
        server = new MainServerTestable();
        server.start();
        clients = new NetworkClientKryo[NUM_CLIENTS];
        for (int i=0; i<NUM_CLIENTS; i++) {
            clients[i] = new NetworkClientKryo();
            SerializationRegister.registerClassesForComponent(clients[i]);
        }
        lobbyID = server.setupLobby(Arrays.asList(clients));
        turnOrder = server.setupGame(lobbyID);

        players = new HashMap<>();
        for (Player p : server.getDataStore().getLobbyByID(lobbyID).getPlayers()) {
            players.put(p.getUid(), p);
        }

        Player attacker = players.get(turnOrder.get(0));
        Player defender = players.get(turnOrder.get(1));
        Lobby l = server.getDataStore().getLobbyByID(lobbyID);
        clients[0].sendMessage(new AttackStartedMessage(lobbyID, attacker.getUid(),
                l.getTerritoriesOccupiedByPlayer(attacker.getUid())[0].getId(),
                l.getTerritoriesOccupiedByPlayer(defender.getUid())[0].getId(), 1));

        Thread.sleep(1500);

        //l = server.getDataStore().getLobbyByID(lobbyID);
        assertTrue(l.attackRunning());
        List<Integer> results = new ArrayList<>();
        results.add(2);
        clients[0].sendMessage(new DiceResultMessage(lobbyID, attacker.getUid(), results, true));
        clients[1].sendMessage(new DefenderDiceCountMessage(lobbyID, defender.getUid(), 1));

        Thread.sleep(1500);
    }

    @Test
    public void testAttackResultWinningDefender() throws InterruptedException {

        Player defender = players.get(turnOrder.get(1));
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

        Thread.sleep(5500);

        Attack attack = server.getDataStore().getLobbyByID(lobbyID).getCurrentAttack();
        assertNull(attack);
        assertEquals(2, l.getTerritoryByID(fromTerritoryID).getArmyCount());
        assertEquals(3, l.getTerritoryByID(toTerritoryID).getArmyCount());
        assertEquals(NUM_CLIENTS, countAttackResultMsgsReceived1.get());
    }

    @Test
    public void testAttackResultWinningAttacker() throws InterruptedException {

        Player defender = players.get(turnOrder.get(1));
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

        Thread.sleep(5500);

        Attack attack = server.getDataStore().getLobbyByID(lobbyID).getCurrentAttack();
        assertNull(attack);
        assertEquals(3, l.getTerritoryByID(fromTerritoryID).getArmyCount());
        assertEquals(2, l.getTerritoryByID(toTerritoryID).getArmyCount());
        assertEquals(NUM_CLIENTS, countAttackResultMsgsReceived2.get());
    }

    @Test
    public void testAttackResultDraw() throws InterruptedException {

        Player defender = players.get(turnOrder.get(1));
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

        Thread.sleep(5500);

        Attack attack = server.getDataStore().getLobbyByID(lobbyID).getCurrentAttack();
        assertNull(attack);
        assertEquals(3, l.getTerritoryByID(fromTerritoryID).getArmyCount());
        assertEquals(2, l.getTerritoryByID(toTerritoryID).getArmyCount());
        assertEquals(NUM_CLIENTS, countAttackResultMsgsReceived3.get());
    }

    @After
    public void tearDown() {
        for (NetworkClientKryo c : clients) {
            c.disconnect();
        }
        server.stop();
    }
}
