package edu.aau.se2.server.data;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class LobbyTest {

    private DataStore ds;

    @Before
    public void setUp() {
        ds = new DataStoreTestClass();
    }

    @After
    public void tearDown() {
        this.ds = null;
    }

    @Test
    public void testConstructor() {
        Lobby lobby = ds.createLobby(new Player(0, "host"));
        assertNotNull(lobby.getPlayers());
        assertFalse(lobby.isStarted());
        assertFalse(lobby.allTerritoriesOccupied());
        assertFalse(lobby.canStartGame());
    }

    @Test
    public void addUser() {
        Lobby lobby = ds.createLobby(new Player(0, "host"));
        lobby.removePlayer(0);
        lobby.setHost(new Player(0, "User0"));
        lobby.addPlayer(new Player(1, "User1"));
        lobby.addPlayer(new Player(2, "User2"));

        assertEquals(3, lobby.getPlayers().size());
        assertEquals(0, lobby.getHost().getUid());
    }
}