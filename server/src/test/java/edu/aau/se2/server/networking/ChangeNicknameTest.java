package edu.aau.se2.server.networking;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.networking.dto.prelobby.ChangeNicknameMessage;
import edu.aau.se2.server.networking.kryonet.NetworkClientKryo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ChangeNicknameTest extends AbstractServerTest {
    private static final int NUM_CLIENTS = 1;
    private Map<NetworkClientKryo, Player> clientPlayers;
    private Player player;
    private AtomicInteger changeNicknameMsgCount = new AtomicInteger(0);
    private ChangeNicknameMessage changeNicknameMessage;

    public ChangeNicknameTest() {
        super(NUM_CLIENTS);
    }

    @Before
    public void setup() throws IOException, TimeoutException {
        server.start();
        clientPlayers = server.connect(Arrays.asList(clients), 5000);
        player = new Player();
    }

    @Test
    public void testChangeNickname() throws InterruptedException {
        int id = clientPlayers.get(clients[0]).getUid();
        String name = clientPlayers.get(clients[0]).getNickname();
        Thread.sleep(2000);

        //check on the player's initial name
        clients[0].sendMessage(new ChangeNicknameMessage(server.getDataStore().getPlayerByID(id)));
        Thread.sleep(500);
        assertEquals(name, server.getDataStore().getPlayerByID(id).getNickname());
        //changing the nickname to "New Player", then check if it not equal to the initial nickname
        clients[0].sendMessage(new ChangeNicknameMessage(id, "New Player"));
        Thread.sleep(1000);
        assertNotEquals(name, server.getDataStore().getPlayerByID(id).getNickname());
    }

    @Test
    public void testChangeNicknameMessageReceived() throws InterruptedException, IOException {
        clients[0].registerCallback(argument -> {
            if (argument instanceof ChangeNicknameMessage) {
                changeNicknameMessage = (ChangeNicknameMessage) argument;
                clients[0].sendMessage(new ChangeNicknameMessage(server.getDataStore().getPlayerByID(1)));
                changeNicknameMsgCount.addAndGet(1);
            }
        });
        clients[0].connect("localhost");
        Thread.sleep(1000);

        clients[0].sendMessage(new ChangeNicknameMessage(server.getDataStore().getPlayerByID(1)));
        Thread.sleep(1000);
        assertEquals(NUM_CLIENTS, changeNicknameMsgCount.get());
    }
    
    @After
    public void tearDown() {
        disconnectAll();
    }
}
