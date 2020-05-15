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
import edu.aau.se2.server.networking.kryonet.NetworkClientKryo;

import static org.junit.Assert.*;

/**
 * Test occupying territories after a "devastating" attack (defender has lost all armies on territory).
 */
public class OccupyTerritoryTest extends AbstractServerTest {
    private static final int NUM_CLIENTS = 3;

    private AtomicInteger countOccupyTerritoryMsgsReceived = new AtomicInteger(0);

    private int lobbyID;
    private List<NetworkClientKryo> turnOrder;
    private Map<NetworkClientKryo, Player> clientPlayers;

    public OccupyTerritoryTest() {
        super(NUM_CLIENTS);
    }

    @Before
    public void setup() throws IOException, TimeoutException {
        // setup game until initial armies are placed
        server.start();
        clientPlayers = server.connect(Arrays.asList(clients), 5000);
        lobbyID = server.setupLobby(Arrays.asList(clients));
        turnOrder = server.startGame(lobbyID, clientPlayers);
        server.placeInitialArmies(lobbyID);
    }

    /**
     * Test the occupation procedure.
     * @throws InterruptedException
     */
    @Test
    public void testOccupy() throws InterruptedException {
        Player attacker = clientPlayers.get(turnOrder.get(0));
        Lobby l = server.getDataStore().getLobbyByID(lobbyID);
        Territory attackerTerritory = l.getTerritoriesOccupiedByPlayer(attacker.getUid())[0];
        Territory defenderTerritory = l.getTerritoriesOccupiedByPlayer(clientPlayers.get(turnOrder.get(1)).getUid())[0];
        int attackerArmyCount = attackerTerritory.getArmyCount();

        for (NetworkClientKryo client : clients) {
            client.registerCallback(msg -> {
                if (msg instanceof OccupyTerritoryMessage) {
                    // check that attacker has moved all but one army to the occupied territory (as specified in message)
                    assertEquals(attackerArmyCount - 1,
                            l.getTerritoryByID(((OccupyTerritoryMessage) msg).getTerritoryID()).getArmyCount());
                    // check that only one army remains on attacking territory
                    assertEquals(1,
                            l.getTerritoryByID(((OccupyTerritoryMessage) msg).getFromTerritoryID()).getArmyCount());
                    countOccupyTerritoryMsgsReceived.addAndGet(1);
                }
            });
        }

        // setup attack to a point where an occupation is required
        server.setupPreOccupy(lobbyID, attackerTerritory.getId(), defenderTerritory.getId());
        clients[0].sendMessage(new OccupyTerritoryMessage(lobbyID, attacker.getUid(), defenderTerritory.getId(), attackerTerritory.getId(),
                attackerTerritory.getArmyCount() - 1));

        Thread.sleep(1500);
        // check that all clients received a valid message
        assertEquals(NUM_CLIENTS, countOccupyTerritoryMsgsReceived.get());
    }

    @After
    public void tearDown() {
        disconnectAll();
    }
}
