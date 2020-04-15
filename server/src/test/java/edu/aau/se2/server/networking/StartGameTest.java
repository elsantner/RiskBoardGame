package edu.aau.se2.server.networking;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import edu.aau.se2.server.MainServer;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.logic.ArmyCountHelper;
import edu.aau.se2.server.networking.dto.ReadyMessage;
import edu.aau.se2.server.networking.dto.StartGameMessage;
import edu.aau.se2.server.networking.kryonet.KryoNetComponent;
import edu.aau.se2.server.networking.kryonet.NetworkClientKryo;

public class StartGameTest {
    private static final int NUM_CLIENTS = 2;

    private ArrayList<AtomicBoolean> clientsStartMsgReceived;
    private MainServer server;

    @Before
    public void setup() {
        clientsStartMsgReceived = new ArrayList<>();
        for (int i=0; i<NUM_CLIENTS; i++) {
            clientsStartMsgReceived.add(new AtomicBoolean(false));
        }
        server = new MainServer();
    }

    @Test
    public void testNetwork_StartGameMessage() throws IOException, InterruptedException {
        startServer();
        startClients();

        Thread.sleep(1500);

        for (int i=0; i<NUM_CLIENTS; i++) {
            Assert.assertTrue(clientsStartMsgReceived.get(i).get());
        }
    }

    private void startServer() throws IOException {
        server.start();
    }

    private void startClients() throws IOException {
        for (int i=0; i<NUM_CLIENTS; i++) {
            NetworkClientKryo client = new NetworkClientKryo();
            SerializationRegister.registerClassesForComponent(client);
            client.connect("localhost");
            int finalI = i;
            client.registerCallback(argument -> {
                Assert.assertTrue(argument instanceof StartGameMessage);
                StartGameMessage msg = (StartGameMessage)argument;
                Assert.assertEquals(NUM_CLIENTS, msg.getPlayers().size());
                Assert.assertEquals(ArmyCountHelper.getStartCount(NUM_CLIENTS), msg.getStartArmyCount());
                clientsStartMsgReceived.get(finalI).set(true);
                }
            );
            client.sendMessage(new ReadyMessage(0, i, true));
        }
    }

    @After
    public void teardown() {
        server.stop();
    }
}