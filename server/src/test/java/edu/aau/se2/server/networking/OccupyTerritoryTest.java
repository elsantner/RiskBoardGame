package edu.aau.se2.server.networking;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import edu.aau.se2.server.data.Lobby;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.data.Territory;
import edu.aau.se2.server.networking.dto.game.OccupyTerritoryMessage;
import edu.aau.se2.server.networking.kryonet.NetworkClientKryo;

import static org.junit.Assert.assertEquals;

;

public class OccupyTerritoryTest {
    private static final int NUM_CLIENTS = 3;

    private AtomicInteger countOccupyTerritoryMsgsReceived = new AtomicInteger(0);

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
    }

    @Test
    public void testOccupy() throws InterruptedException {
        Player attacker = players.get(turnOrder.get(0));
        Lobby l = server.getDataStore().getLobbyByID(lobbyID);
        Territory attackerTerritory = l.getTerritoriesOccupiedByPlayer(attacker.getUid())[0];
        Territory defenderTerritory = l.getTerritoriesOccupiedByPlayer(players.get(turnOrder.get(1)).getUid())[0];
        int attackerArmyCount = attackerTerritory.getArmyCount();

        for (NetworkClientKryo client : clients) {
            client.registerCallback(msg -> {
                if (msg instanceof OccupyTerritoryMessage) {
                    assertEquals(attackerArmyCount - 1,
                            l.getTerritoryByID(((OccupyTerritoryMessage) msg).getTerritoryID()).getArmyCount());
                    assertEquals(1,
                            l.getTerritoryByID(((OccupyTerritoryMessage) msg).getFromTerritoryID()).getArmyCount());
                    countOccupyTerritoryMsgsReceived.addAndGet(1);
                }
            });
        }

        server.setupPreOccupy(lobbyID, attackerTerritory.getId(), defenderTerritory.getId());
        clients[0].sendMessage(new OccupyTerritoryMessage(lobbyID, attacker.getUid(), defenderTerritory.getId(), attackerTerritory.getId(),
                attackerTerritory.getArmyCount() - 1));

        Thread.sleep(1500);
        assertEquals(NUM_CLIENTS, countOccupyTerritoryMsgsReceived.get());
    }

    @After
    public void tearDown() {
        for (NetworkClientKryo c : clients) {
            c.disconnect();
        }
        server.stop();
    }
}
