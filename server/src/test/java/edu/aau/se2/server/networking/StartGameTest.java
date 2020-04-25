package edu.aau.se2.server.networking;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import edu.aau.se2.server.MainServer;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.data.Territory;
import edu.aau.se2.server.logic.ArmyCountHelper;
import edu.aau.se2.server.networking.dto.game.ArmyPlacedMessage;
import edu.aau.se2.server.networking.dto.prelobby.ConnectedMessage;
import edu.aau.se2.server.networking.dto.lobby.CreateLobbyMessage;
import edu.aau.se2.server.networking.dto.game.InitialArmyPlacingMessage;
import edu.aau.se2.server.networking.dto.lobby.JoinedLobbyMessage;
import edu.aau.se2.server.networking.dto.game.NextTurnMessage;
import edu.aau.se2.server.networking.dto.lobby.ReadyMessage;
import edu.aau.se2.server.networking.dto.lobby.RequestJoinLobbyMessage;
import edu.aau.se2.server.networking.dto.game.StartGameMessage;
import edu.aau.se2.server.networking.kryonet.NetworkClientKryo;

public class StartGameTest {
    private static final int NUM_CLIENTS = 4;

    private ArrayList<NetworkClientKryo> clients;
    private MainServer server;

    private ArrayList<AtomicBoolean> clientsStartMsgReceived;
    private ArrayList<AtomicBoolean> clientsConnectedMsgReceived;
    private ArrayList<AtomicBoolean> clientsInitialArmyPlacingMsgReceived;
    private AtomicInteger armiesPlaced;
    private AtomicInteger firstTurnPlayerID;

    private List<Integer> turnOrder;
    private int currentTurnIndex;
    private ArrayList<Integer> clientArmyCount;
    private ArrayList<Territory> unoccupiedTerritories;
    private Map<Integer, ArrayList<Territory>> playerOccupiedTerritories;
    private Player[] clientPlayers;
    private JoinedLobbyMessage joinedLobbyMessage;

    @Before
    public void setup() {
        currentTurnIndex = 0;
        clientsStartMsgReceived = new ArrayList<>();
        clientsInitialArmyPlacingMsgReceived = new ArrayList<>();
        clientsConnectedMsgReceived = new ArrayList<>();
        unoccupiedTerritories = new ArrayList<>();
        clientArmyCount = new ArrayList<>();
        clients = new ArrayList<>();
        clientPlayers = new Player[NUM_CLIENTS];

        for (int i=0; i<NUM_CLIENTS; i++) {
            clientArmyCount.add(0);
        }

        playerOccupiedTerritories = new HashMap<>();
        armiesPlaced = new AtomicInteger(0);
        firstTurnPlayerID = new AtomicInteger(-1);

        for (int i=0; i<42; i++) {
            unoccupiedTerritories.add(new Territory(i));
        }
        for (int i=0; i<NUM_CLIENTS; i++) {
            clientsStartMsgReceived.add(new AtomicBoolean(false));
            clientsConnectedMsgReceived.add(new AtomicBoolean(false));
            clientsInitialArmyPlacingMsgReceived.add(new AtomicBoolean(false));
        }
        server = new MainServer();
    }

    @Test
    public void testStartGameSetup() throws IOException, InterruptedException {
        startServer();
        Thread.sleep(1000);
        startClients();
        Thread.sleep(1000);
        setupLobby();
        Thread.sleep(3000);
        startGame();

        Thread.sleep(5000);

        for (int i=0; i<NUM_CLIENTS; i++) {
            Assert.assertTrue(clientsConnectedMsgReceived.get(i).get());
            Assert.assertTrue(clientsStartMsgReceived.get(i).get());
        }
        // check if all armies were placed
        Assert.assertEquals(ArmyCountHelper.getStartCount(NUM_CLIENTS)*NUM_CLIENTS, armiesPlaced.get());
        Assert.assertEquals((int)turnOrder.get(0), firstTurnPlayerID.get());
    }

    private void startGame() {
        for (int i=0; i<NUM_CLIENTS; i++) {
            clients.get(i).sendMessage(new ReadyMessage(joinedLobbyMessage.getLobbyID(), clientPlayers[i].getUid(), true));
        }
    }

    private void setupLobby() throws InterruptedException {
        clients.get(0).sendMessage(new CreateLobbyMessage(clientPlayers[0].getUid()));
        Thread.sleep(1000);
        for (int i=1; i<NUM_CLIENTS; i++) {
            clients.get(i).sendMessage(new RequestJoinLobbyMessage(joinedLobbyMessage.getLobbyID(), clientPlayers[i].getUid()));
        }
    }

    private void startServer() throws IOException {
        server.start();
    }

    private void startClients() throws IOException {
        for (int i=0; i<NUM_CLIENTS; i++) {
            NetworkClientKryo client = new NetworkClientKryo();
            clients.add(client);
            SerializationRegister.registerClassesForComponent(client);
            int finalI = i;
            client.registerCallback(argument -> {
                if (argument instanceof ConnectedMessage) {
                    handleConnectedMessage((ConnectedMessage)argument, finalI);
                }
                else if (argument instanceof StartGameMessage) {
                    Assert.assertTrue(clientsConnectedMsgReceived.get(finalI).get());
                    handleStartGameMessage((StartGameMessage) argument, finalI);
                }
                else if (argument instanceof InitialArmyPlacingMessage) {
                    handleInitialArmyPlacingMessage((InitialArmyPlacingMessage) argument, finalI);
                }
                else if (argument instanceof ArmyPlacedMessage) {
                    handleArmyPlacedMessage((ArmyPlacedMessage) argument, finalI);
                }
                else if (argument instanceof NextTurnMessage) {
                    handleNextTurnMessage((NextTurnMessage) argument);
                }
                else if (argument instanceof JoinedLobbyMessage) {
                    joinedLobbyMessage = (JoinedLobbyMessage) argument;
                }
            });
            client.connect("localhost");
        }
    }

    private synchronized void handleNextTurnMessage(NextTurnMessage msg) {
        firstTurnPlayerID.set(msg.getPlayerToActID());
    }

    private synchronized void handleConnectedMessage(ConnectedMessage msg, int clientIndex) {
        clientPlayers[clientIndex] = msg.getPlayer();
        playerOccupiedTerritories.put(clientPlayers[clientIndex].getUid(), new ArrayList<>());
        clientsConnectedMsgReceived.get(clientIndex).set(true);
    }

    private synchronized void handleStartGameMessage(StartGameMessage msg, int clientIndex) {
        // check if all clients are in the game
        Assert.assertEquals(NUM_CLIENTS, msg.getPlayers().size());
        // check if the right amount of initial armies are transmitted
        Assert.assertEquals(ArmyCountHelper.getStartCount(NUM_CLIENTS), msg.getStartArmyCount());
        clientArmyCount.set(clientIndex, msg.getStartArmyCount());
        clientsStartMsgReceived.get(clientIndex).set(true);
    }

    private synchronized void handleInitialArmyPlacingMessage(InitialArmyPlacingMessage msg, int clientIndex) {
        // StartGameMessage must have already been received
        Assert.assertTrue(clientsStartMsgReceived.get(clientIndex).get());
        turnOrder = msg.getPlayerOrder();
        clientsInitialArmyPlacingMsgReceived.get(clientIndex).set(true);
        // let first client send ArmyPlacedMessage
        sendArmyPlaceIfClientsTurn(clientPlayers[clientIndex].getUid(), -1, clients.get(clientIndex));
    }

    private synchronized void handleArmyPlacedMessage(ArmyPlacedMessage msg, int clientIndex) {
        // do once per ArmyPlacedMessage
        if (msg.getFromPlayerID() == clientPlayers[clientIndex].getUid()) {
            // increment armies placed count
            armiesPlaced.getAndAdd(1);
            // check if remaining army count is successfully calculated and returned by the server
            clientArmyCount.set(clientIndex, clientArmyCount.get(clientIndex)-1);
            Assert.assertEquals((int)clientArmyCount.get(clientIndex), msg.getArmyCountRemaining());
        }
        // let player to act do their turn
        sendArmyPlaceIfClientsTurn(clientPlayers[clientIndex].getUid(), msg.getFromPlayerID(), clients.get(clientIndex));
    }

    private synchronized void sendArmyPlaceIfClientsTurn(int clientPlayerID, int prevTurnPlayerID, NetworkClientKryo client) {
        // get next current turn player based on previously received ArmyPlacedMessage.fromPlayerID
        currentTurnIndex = turnOrder.indexOf(prevTurnPlayerID)+1;
        currentTurnIndex %= NUM_CLIENTS;

        // send army placed message if it's this players turn
        if (clientPlayerID == turnOrder.get(currentTurnIndex)) {
            int territoryID = getNextTerritoryToPlaceArmy(clientPlayerID);
            client.sendMessage(new ArmyPlacedMessage(joinedLobbyMessage.getLobbyID(), clientPlayerID, territoryID, 1));
        }
    }

    private synchronized int getNextTerritoryToPlaceArmy(int clientPlayerID) {
        if (!unoccupiedTerritories.isEmpty()) {
            Territory t = unoccupiedTerritories.remove(0);
            playerOccupiedTerritories.get(clientPlayerID).add(t);
            return t.getId();
        }
        else {
            Random rand = new Random();
            ArrayList<Territory> thisPlayersTerritories = playerOccupiedTerritories.get(clientPlayerID);
            return thisPlayersTerritories.get(rand.nextInt(thisPlayersTerritories.size()-1)).getId();
        }
    }

    @After
    public void teardown() {
        for (NetworkClientKryo c: clients) {
            c.disconnect();
        }
        server.stop();
    }
}