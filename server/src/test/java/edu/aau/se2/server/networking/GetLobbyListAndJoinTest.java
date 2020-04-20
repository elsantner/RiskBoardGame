package edu.aau.se2.server.networking;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import edu.aau.se2.server.MainServer;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.networking.dto.ConnectedMessage;
import edu.aau.se2.server.networking.dto.CreateLobbyMessage;
import edu.aau.se2.server.networking.dto.JoinedLobbyMessage;
import edu.aau.se2.server.networking.dto.LeftLobbyMessage;
import edu.aau.se2.server.networking.dto.LobbyListMessage;
import edu.aau.se2.server.networking.dto.RequestJoinLobbyMessage;
import edu.aau.se2.server.networking.dto.RequestLeaveLobby;
import edu.aau.se2.server.networking.dto.RequestLobbyListMessage;
import edu.aau.se2.server.networking.kryonet.NetworkClientKryo;

public class GetLobbyListAndJoinTest {
    private static final int NUM_HOSTS = 3;

    private NetworkClientKryo client;
    private MainServer server;
    private Player[] hostPlayers;
    private Player listRequestPlayer;
    private LobbyListMessage listMessage;
    private JoinedLobbyMessage joinedMessage;
    private LeftLobbyMessage leftLobbyMessage;
    private int[] lobbyIDs = new int[NUM_HOSTS];
    private NetworkClientKryo[] hosts = new NetworkClientKryo[NUM_HOSTS];

    @Before
    public void setup() {
        server = new MainServer();
        hostPlayers = new Player[NUM_HOSTS];
    }

    @Test
    public void testGetLobbyListAndJoin() throws IOException, InterruptedException {
        startServer();
        Thread.sleep(1000);
        startClients();
        Thread.sleep(1000);
        requestListAndJoin();
        Thread.sleep(1000);

        Assert.assertEquals(NUM_HOSTS, listMessage.getLobbies().size());
        Assert.assertEquals(hostPlayers[0].getUid(), joinedMessage.getHost().getUid());
        Assert.assertEquals(2, joinedMessage.getPlayers().size());

        leaveLobbyRegularly();
        Thread.sleep(1000);
        requestListAndJoin();
        Thread.sleep(1000);

        Assert.assertFalse(leftLobbyMessage.isWasClosed());
        Assert.assertEquals(NUM_HOSTS, listMessage.getLobbies().size());
        Assert.assertEquals(hostPlayers[0].getUid(), joinedMessage.getHost().getUid());
        Assert.assertEquals(2, joinedMessage.getPlayers().size());

        leaveLobbyDisconnect();
        Thread.sleep(1000);
        requestListAndJoin();
        Thread.sleep(1000);

        Assert.assertEquals(NUM_HOSTS, listMessage.getLobbies().size());
        Assert.assertEquals(hostPlayers[0].getUid(), joinedMessage.getHost().getUid());
        Assert.assertEquals(2, joinedMessage.getPlayers().size());

        leaveLobbyHostRegularly();
        Thread.sleep(1000);
        requestList();
        Thread.sleep(1000);

        Assert.assertEquals(NUM_HOSTS-1, listMessage.getLobbies().size());

        leaveLobbyHostDisconnect();
        Thread.sleep(1000);
        requestList();
        Thread.sleep(1000);

        Assert.assertEquals(NUM_HOSTS-2, listMessage.getLobbies().size());
    }

    private void leaveLobbyHostDisconnect() {
        hosts[1].disconnect();
    }

    private void requestList() {
        client.registerCallback(argument -> {
            if (argument instanceof LobbyListMessage) {
                listMessage = ((LobbyListMessage)argument);
            }
        });
        client.sendMessage(new RequestLobbyListMessage(listRequestPlayer.getUid()));
    }

    private void leaveLobbyHostRegularly() {
        hosts[0].sendMessage(new RequestLeaveLobby(lobbyIDs[0], hostPlayers[0].getUid()));
    }

    private void leaveLobbyDisconnect() {
        client.disconnect();
    }

    private void leaveLobbyRegularly() {
        client.registerCallback(argument -> {
            if (argument instanceof LeftLobbyMessage) {
                leftLobbyMessage = (LeftLobbyMessage)argument;
                client.disconnect();
            }
        });
        client.sendMessage(new RequestLeaveLobby(joinedMessage.getLobbyID(), listRequestPlayer.getUid()));
    }

    private void requestListAndJoin() throws IOException {
        client = new NetworkClientKryo();
        SerializationRegister.registerClassesForComponent(client);

        client.registerCallback(argument -> {
            if (argument instanceof ConnectedMessage) {
                listRequestPlayer = ((ConnectedMessage)argument).getPlayer();
                client.sendMessage(new RequestLobbyListMessage(listRequestPlayer.getUid()));
            }
            else if (argument instanceof LobbyListMessage) {
                listMessage = ((LobbyListMessage)argument);
                client.sendMessage(new RequestJoinLobbyMessage(
                        listMessage.getLobbies().get(0).getLobbyID(), listRequestPlayer.getUid()));
            }
            else if (argument instanceof JoinedLobbyMessage) {
                joinedMessage = ((JoinedLobbyMessage)argument);
            }
        });
        client.connect("localhost");
    }

    private void startServer() throws IOException {
        server.start();
    }

    private void startClients() throws IOException {
        for (int i=0; i<NUM_HOSTS; i++) {
            NetworkClientKryo client = new NetworkClientKryo();
            hosts[i] = client;
            SerializationRegister.registerClassesForComponent(client);
            int finalI = i;
            client.registerCallback(argument -> {
                if (argument instanceof ConnectedMessage) {
                    handleConnectedMessage((ConnectedMessage)argument, finalI);
                    client.sendMessage(new CreateLobbyMessage(hostPlayers[finalI].getUid()));
                }
                else if (argument instanceof JoinedLobbyMessage) {
                    lobbyIDs[finalI] = ((JoinedLobbyMessage)argument).getLobbyID();
                }
            });
            client.connect("localhost");
        }
    }

    private synchronized void handleConnectedMessage(ConnectedMessage msg, int clientIndex) {
        hostPlayers[clientIndex] = msg.getPlayer();
    }

    @After
    public void teardown() {
        server.stop();
    }
}
