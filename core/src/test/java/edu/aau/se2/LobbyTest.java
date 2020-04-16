package edu.aau.se2;

import com.badlogic.gdx.Gdx;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.io.IOException;

import edu.aau.se2.server.networking.dto.TextMessage;
import edu.aau.se2.server.networking.kryonet.NetworkClientKryo;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(GdxTestRunner.class)
public class LobbyTest {

    private Lobby lobby;
    private NetworkClientKryo mockedClient = mock(NetworkClientKryo.class);

    @Before
    public void setUp() throws Exception {
        this.lobby = new Lobby();
        lobby.setNetworkClientKryo(mockedClient);
    }

    @After
    public void tearDown() throws Exception {
        this.lobby = null;
        this.mockedClient = null;
    }

    @Test
    public void testCreateLobbyConnect() throws IOException {
        lobby.createLobby();
        verify(mockedClient).connect(Lobby.getHOST());
    }

    @Test
    public void testCreateLobbyRegisterClasses() {
        lobby.createLobby();
        verify(mockedClient).registerClass(TextMessage.class);
    }

    @Test
    public void testCreateLobbySendMessage() {
        lobby.createLobby();
        verify(mockedClient).sendMessage(any());
    }

    @Test
    public void testCreateLobbyRegisterCallback() {
        lobby.createLobby();
        verify(mockedClient).registerCallback(any());
    }

    @Test(expected = NullPointerException.class)
    public void testNetworkClientNull() {
        lobby.setNetworkClientKryo(null);
        lobby.createLobby();
    }

}