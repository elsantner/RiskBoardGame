package edu.aau.se2.server.networking;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import edu.aau.se2.server.MainServer;
import edu.aau.se2.server.data.Territory;
import edu.aau.se2.server.logic.ArmyCountHelper;
import edu.aau.se2.server.networking.dto.ArmyPlacedMessage;
import edu.aau.se2.server.networking.dto.InitialArmyPlacingMessage;
import edu.aau.se2.server.networking.dto.ReadyMessage;
import edu.aau.se2.server.networking.dto.StartGameMessage;
import edu.aau.se2.server.networking.kryonet.NetworkClientKryo;

public class StartGameTest {
    private static final int NUM_CLIENTS = 2;

    private ArrayList<AtomicBoolean> clientsStartMsgReceived;
    private ArrayList<AtomicBoolean> clientsInitialArmyPlacingMsgReceived;
    private AtomicInteger armiesPlaced;
    private MainServer server;
    private ArrayList<Integer> turnOrder;
    private int currentTurnIndex;
    private ArrayList<Integer> clientArmyCount;

    private ArrayList<Territory> unoccupiedTerritories;
    private ArrayList<ArrayList<Territory>> playerOccupiedTerritories;
    private ArrayList<NetworkClientKryo> clients;

    @Before
    public void setup() {
        currentTurnIndex = 0;
        clientsStartMsgReceived = new ArrayList<>();
        clientsInitialArmyPlacingMsgReceived = new ArrayList<>();
        unoccupiedTerritories = new ArrayList<>();
        clientArmyCount = new ArrayList<>();
        clients = new ArrayList<>();

        for (int i=0; i<NUM_CLIENTS; i++) {
            clientArmyCount.add(0);
        }

        playerOccupiedTerritories = new ArrayList<>();
        armiesPlaced = new AtomicInteger(0);

        for (int i=0; i<42; i++) {
            unoccupiedTerritories.add(new Territory(i+1));
        }
        for (int i=0; i<NUM_CLIENTS; i++) {
            playerOccupiedTerritories.add(new ArrayList<>());
        }

        for (int i=0; i<NUM_CLIENTS; i++) {
            clientsStartMsgReceived.add(new AtomicBoolean(false));
            clientsInitialArmyPlacingMsgReceived.add(new AtomicBoolean(false));
        }
        server = new MainServer();
    }

    @Test
    public void testNetwork_StartGameMessage() throws IOException, InterruptedException {
        startServer();
        startClients();

        Thread.sleep(5000);

        for (int i=0; i<NUM_CLIENTS; i++) {
            Assert.assertTrue(clientsStartMsgReceived.get(i).get());
        }
        // check if all armies were placed
        Assert.assertEquals(ArmyCountHelper.getStartCount(NUM_CLIENTS)*NUM_CLIENTS, armiesPlaced.get());
    }

    private void startServer() throws IOException {
        server.start();
    }

    private void startClients() throws IOException {
        for (int i=0; i<NUM_CLIENTS; i++) {
            NetworkClientKryo client = new NetworkClientKryo();
            clients.add(client);
            SerializationRegister.registerClassesForComponent(client);
            client.connect("localhost");
            int finalI = i;
            client.registerCallback(argument -> {
                if (argument instanceof StartGameMessage) {
                    StartGameMessage msg = (StartGameMessage) argument;
                    // check if all clients are in the game
                    Assert.assertEquals(NUM_CLIENTS, msg.getPlayers().size());
                    // check if the right amount of initial armies are transmitted
                    Assert.assertEquals(ArmyCountHelper.getStartCount(NUM_CLIENTS), msg.getStartArmyCount());
                    clientArmyCount.set(finalI, msg.getStartArmyCount());
                    clientsStartMsgReceived.get(finalI).set(true);
                }
                else if (argument instanceof InitialArmyPlacingMessage) {
                    InitialArmyPlacingMessage msg = (InitialArmyPlacingMessage) argument;
                    // StartGameMessage must have already been received
                    Assert.assertTrue(clientsStartMsgReceived.get(finalI).get());
                    turnOrder = msg.getPlayerOrder();
                    clientsInitialArmyPlacingMsgReceived.get(finalI).set(true);
                    // let first client send ArmyPlacedMessage
                    sendArmyPlaceIfClientsTurn(finalI, -1, client);
                }
                else if (argument instanceof ArmyPlacedMessage) {
                    ArmyPlacedMessage msg = (ArmyPlacedMessage) argument;
                    // do once per ArmyPlacedMessage
                    if (msg.getFromPlayerID() == finalI) {
                        // increment armies placed count
                        armiesPlaced.getAndAdd(1);
                        // check if remaining army count is successfully calculated and returned by the server
                        clientArmyCount.set(finalI, clientArmyCount.get(finalI)-1);
                        Assert.assertEquals((int)clientArmyCount.get(finalI), msg.getArmyCountRemaining());
                    }
                    // let player to act do their turn
                    sendArmyPlaceIfClientsTurn(finalI, msg.getFromPlayerID(), client);
                }
            });
        }

        for (int i=0; i<NUM_CLIENTS; i++) {
            clients.get(i).sendMessage(new ReadyMessage(0, i, true));
        }
    }

    private synchronized void sendArmyPlaceIfClientsTurn(int clientPlayerID, int prevTurnPlayerID, NetworkClientKryo client) {
        // get next current turn player based on previously received ArmyPlacedMessage.fromPlayerID
        currentTurnIndex = turnOrder.indexOf(prevTurnPlayerID)+1;
        currentTurnIndex %= NUM_CLIENTS;

        // send army placed message if it's this players turn
        if (clientPlayerID == turnOrder.get(currentTurnIndex)) {
            int territoryID = getNextTerritoryToPlaceArmy(clientPlayerID);
            client.sendMessage(new ArmyPlacedMessage(0, clientPlayerID, territoryID, 1));
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