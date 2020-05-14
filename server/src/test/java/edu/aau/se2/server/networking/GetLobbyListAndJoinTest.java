package edu.aau.se2.server.networking;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.networking.dto.lobby.CreateLobbyMessage;
import edu.aau.se2.server.networking.dto.lobby.JoinedLobbyMessage;
import edu.aau.se2.server.networking.dto.lobby.LeftLobbyMessage;
import edu.aau.se2.server.networking.dto.lobby.RequestJoinLobbyMessage;
import edu.aau.se2.server.networking.dto.lobby.RequestLeaveLobby;
import edu.aau.se2.server.networking.dto.prelobby.ConnectedMessage;
import edu.aau.se2.server.networking.dto.prelobby.LobbyListMessage;
import edu.aau.se2.server.networking.dto.prelobby.RequestLobbyListMessage;
import edu.aau.se2.server.networking.kryonet.NetworkClientKryo;

import static org.junit.Assert.*;

/**
 * Test requesting lobby list, joining lobbies based on lobby list and leaving lobbies regularly or by disconnecting.
 */
public class GetLobbyListAndJoinTest extends AbstractServerTest {
    private static final int NUM_HOSTS = 3;

    private NetworkClientKryo joiner;
    private Player[] hostPlayers;
    private Player listRequestPlayer;
    private LobbyListMessage listMessage;
    private JoinedLobbyMessage joinedMessage;
    private LeftLobbyMessage leftLobbyMessageClient;
    private LeftLobbyMessage leftLobbyMessageHost;
    private int[] lobbyIDs = new int[NUM_HOSTS];

    public GetLobbyListAndJoinTest() {
        super(NUM_HOSTS);
    }

    @Before
    public void setup() throws IOException {
        server.start();
        joiner = new NetworkClientKryo();
        SerializationRegister.registerClassesForComponent(joiner);
        hostPlayers = new Player[NUM_HOSTS];
    }

    @Test
    public void testGetLobbyListAndJoin() throws IOException, InterruptedException {
        startClients();
        Thread.sleep(1000);
        requestListAndJoin();
        Thread.sleep(1000);

        // check that all lobbies were created successfully
        assertEquals(NUM_HOSTS, listMessage.getLobbies().size());
        // check that host is set correctly
        assertEquals(hostPlayers[0].getUid(), joinedMessage.getHost().getUid());
        // check that client joined lobby successfully
        assertEquals(2, joinedMessage.getPlayers().size());
        assertEquals(2, server.getDataStore().getLobbyByID(joinedMessage.getLobbyID()).getPlayers().size());

        leaveLobbyRegularly();
        Thread.sleep(1000);
        // check that client left lobby successfully
        assertEquals(1, server.getDataStore().getLobbyByID(joinedMessage.getLobbyID()).getPlayers().size());
        requestListAndJoin();
        Thread.sleep(1000);

        // check that regular leave was correctly registered
        assertFalse(leftLobbyMessageClient.isWasClosed());
        // check that lobby is still open & host is still correctly set
        assertEquals(NUM_HOSTS, listMessage.getLobbies().size());
        assertEquals(hostPlayers[0].getUid(), joinedMessage.getHost().getUid());
        // check that client re-joined successfully
        assertEquals(2, server.getDataStore().getLobbyByID(joinedMessage.getLobbyID()).getPlayers().size());
        assertEquals(2, joinedMessage.getPlayers().size());

        leaveLobbyDisconnect();
        Thread.sleep(1000);
        // check that client left lobby successfully
        assertEquals(1, server.getDataStore().getLobbyByID(joinedMessage.getLobbyID()).getPlayers().size());
        requestListAndJoin();
        Thread.sleep(1000);

        // check that lobby is still open after disconnect of non-host
        assertEquals(NUM_HOSTS, listMessage.getLobbies().size());
        assertEquals(hostPlayers[0].getUid(), joinedMessage.getHost().getUid());
        // check that player re-joined successfully after disconnect
        assertEquals(2, joinedMessage.getPlayers().size());
        assertEquals(2, server.getDataStore().getLobbyByID(joinedMessage.getLobbyID()).getPlayers().size());

        leaveLobbyHostRegularly();
        Thread.sleep(1000);
        requestList();
        Thread.sleep(1000);

        // check that host received correct message
        assertTrue(leftLobbyMessageHost.isWasClosed());
        // check that lobby was closed after host left regularly
        assertEquals(NUM_HOSTS-1, listMessage.getLobbies().size());
        assertEquals(NUM_HOSTS-1, server.getDataStore().getJoinableLobbyList().size());

        leaveLobbyHostDisconnect();
        Thread.sleep(1000);
        requestList();
        Thread.sleep(1000);

        assertEquals(NUM_HOSTS-2, listMessage.getLobbies().size());
        assertEquals(NUM_HOSTS-2, server.getDataStore().getJoinableLobbyList().size());
    }

    private void leaveLobbyHostDisconnect() {
        clients[1].disconnect();
    }

    private void requestList() {
        joiner.registerCallback(argument -> {
            if (argument instanceof LobbyListMessage) {
                listMessage = ((LobbyListMessage)argument);
            }
        });
        joiner.sendMessage(new RequestLobbyListMessage(listRequestPlayer.getUid()));
    }

    private void leaveLobbyHostRegularly() {
        clients[0].registerCallback(argument -> {
            if (argument instanceof LeftLobbyMessage) {
                leftLobbyMessageHost = (LeftLobbyMessage)argument;
            }
        });
        clients[0].sendMessage(new RequestLeaveLobby(lobbyIDs[0], hostPlayers[0].getUid()));
    }

    private void leaveLobbyDisconnect() {
        joiner.disconnect();
    }

    private void leaveLobbyRegularly() {
        joiner.registerCallback(argument -> {
            if (argument instanceof LeftLobbyMessage) {
                leftLobbyMessageClient = (LeftLobbyMessage)argument;
                joiner.disconnect();
            }
        });
        joiner.sendMessage(new RequestLeaveLobby(joinedMessage.getLobbyID(), listRequestPlayer.getUid()));
    }

    private void requestListAndJoin() throws IOException {
        joiner.registerCallback(argument -> {
            if (argument instanceof ConnectedMessage) {
                listRequestPlayer = ((ConnectedMessage)argument).getPlayer();
                joiner.sendMessage(new RequestLobbyListMessage(listRequestPlayer.getUid()));
            }
            else if (argument instanceof LobbyListMessage) {
                listMessage = ((LobbyListMessage)argument);
                joiner.sendMessage(new RequestJoinLobbyMessage(
                        listMessage.getLobbies().get(0).getLobbyID(), listRequestPlayer.getUid()));
            }
            else if (argument instanceof JoinedLobbyMessage) {
                joinedMessage = ((JoinedLobbyMessage)argument);
            }
        });
        joiner.connect("localhost");
    }

    private void startClients() throws IOException {
        for (int i=0; i<NUM_HOSTS; i++) {
            int finalI = i;
            clients[i].registerCallback(argument -> {
                if (argument instanceof ConnectedMessage) {
                    handleConnectedMessage((ConnectedMessage)argument, finalI);
                    clients[finalI].sendMessage(new CreateLobbyMessage(hostPlayers[finalI].getUid()));
                }
                else if (argument instanceof JoinedLobbyMessage) {
                    lobbyIDs[finalI] = ((JoinedLobbyMessage)argument).getLobbyID();
                }
            });
            clients[i].connect("localhost");
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
