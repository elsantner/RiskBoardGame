package edu.aau.se2.server.networking;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.networking.dto.prelobby.ChangeNicknameMessage;
import edu.aau.se2.server.networking.kryonet.NetworkClientKryo;
import static org.junit.Assert.assertNotEquals;

public class ChangeNicknameTest extends AbstractServerTest {
    private static final int NUM_CLIENTS = 1;
    private Map<NetworkClientKryo, Player> clientPlayers;

    public ChangeNicknameTest() {
        super(NUM_CLIENTS);
    }

    @Before
    public void setup() throws IOException, TimeoutException {
        server.start();
        clientPlayers = server.connect(Arrays.asList(clients), 5000);
    }

    @Test
    public void testChangeNickname() throws InterruptedException {
        int id = clientPlayers.get(clients[0]).getUid();
        String name = clientPlayers.get(clients[0]).getNickname();
        //changing the nickname to "New Player", then check if it not equal to the initial nickname
        clients[0].sendMessage(new ChangeNicknameMessage(id, "New Player"));
        Thread.sleep(2000);
        assertNotEquals(name, server.getDataStore().getPlayerByID(id).getNickname());
    }

    @After
    public void tearDown() {
        disconnectAll();
    }
}
