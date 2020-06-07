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

import edu.aau.se2.server.data.Lobby;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.data.Territory;
import edu.aau.se2.server.networking.dto.BaseMessage;
import edu.aau.se2.server.networking.dto.game.AccuseCheaterMessage;
import edu.aau.se2.server.networking.dto.game.AttackResultMessage;
import edu.aau.se2.server.networking.dto.game.AttackStartedMessage;
import edu.aau.se2.server.networking.dto.game.DefenderDiceCountMessage;
import edu.aau.se2.server.networking.dto.game.DiceResultMessage;
import edu.aau.se2.server.networking.kryonet.NetworkClientKryo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test starting attacks on territories.
 */
public class CheatingTest extends AbstractServerTest {
    private static final int NUM_CLIENTS = 3;

    private AtomicInteger countAttackResultMsg = new AtomicInteger(0);

    private int lobbyID;
    private List<NetworkClientKryo> turnOrder;
    private Map<NetworkClientKryo, Player> clientPlayers;
    private Map<Integer, List<Territory>> playerOccupiedTerritories;

    public CheatingTest() {
        super(NUM_CLIENTS);
    }

    @Before
    public void setup() throws IOException, TimeoutException, InterruptedException {
        // setup game until initial armies are placed
        server.start();
        clientPlayers = server.connect(Arrays.asList(clients), 5000);
        lobbyID = server.setupLobby(Arrays.asList(clients));
        turnOrder = server.startGame(lobbyID, clientPlayers);

        playerOccupiedTerritories = server.placeInitialArmies(lobbyID);
    }


    @Test
    public void testAccuseNotCheated() throws InterruptedException {
        setUpAttack(1, false);
        Thread.sleep(1500);

        Player defender = clientPlayers.get(turnOrder.get(1));

        for(int i = 0; i < clients.length; i++){
            clients[i].registerCallback(argument -> {
                if (argument instanceof AttackResultMessage){
                    assertEquals(0, ((AttackResultMessage) argument).getArmiesLostAttacker());
                    assertEquals(1, ((AttackResultMessage) argument).getArmiesLostDefender());
                    countAttackResultMsg.addAndGet(1);
                }
            });
        }
        clients[1].sendMessage(new AccuseCheaterMessage(lobbyID, defender.getUid()));
        Thread.sleep(1000);

        assertEquals(NUM_CLIENTS, countAttackResultMsg.get());
    }

    @Test
    public void testAccuseCheated() throws InterruptedException {
        setUpAttack(2, true);
        Thread.sleep(1500);

        Player defender = clientPlayers.get(turnOrder.get(1));

        for(int i = 0; i < clients.length; i++){
            clients[i].registerCallback(argument -> {
                if (argument instanceof AttackResultMessage){
                    assertEquals(2, ((AttackResultMessage) argument).getArmiesLostAttacker());
                    assertEquals(0, ((AttackResultMessage) argument).getArmiesLostDefender());
                    countAttackResultMsg.addAndGet(1);
                }
            });
        }
        clients[1].sendMessage(new AccuseCheaterMessage(lobbyID, defender.getUid()));
        Thread.sleep(1000);

        assertEquals(NUM_CLIENTS, countAttackResultMsg.get());
    }


    public void setUpAttack(int diceCount, boolean cheated) throws InterruptedException {
        Player attacker = clientPlayers.get(turnOrder.get(0));
        Player defender = clientPlayers.get(turnOrder.get(1));

        clients[0].sendMessage(new AttackStartedMessage(lobbyID, attacker.getUid(),
                playerOccupiedTerritories.get(attacker.getUid()).get(0).getId(),
                playerOccupiedTerritories.get(defender.getUid()).get(0).getId(), diceCount));

        Thread.sleep(1500);


        List<Integer> results = new ArrayList<>();
        for (int i = 0; i < diceCount; i++) {
            results.add(i+1);
        }
        clients[0].sendMessage(new DiceResultMessage(lobbyID, attacker.getUid(), results, cheated, true));
    }

    @After
    public void tearDown() {
        disconnectAll();
    }
}
