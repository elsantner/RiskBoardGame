package edu.aau.se2.server;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import edu.aau.se2.server.Lobby;

import static org.junit.Assert.*;

public class LobbyTest {

    private Lobby lobby;

    @Before
    public void setUp() throws Exception {
        this.lobby = new Lobby("hostname", 1);
    }

    @After
    public void tearDown() throws Exception {
        this.lobby = null;
    }

    @Test
    public void testConstructor() {
        Lobby lobby = new Lobby("test", 0);
        assertEquals("test", lobby.getUser(0).getName());
        assertEquals(0, lobby.getLobbyID());
        assertNotNull(lobby.getUsers());
        assertTrue(lobby.getUser(0).isHost());
    }

    @Test
    public void addUser() {
        lobby.addUser(new User());
        lobby.addUser(new User("User"));
        lobby.addUser(new User("User2", false));

        ArrayList<User> list = (ArrayList<User>) lobby.getUsers();
        assertNotNull(list.get(1));
        assertEquals("User", list.get(2).getName());
        assertFalse(list.get(3).isReady());
    }

    @Test
    public void getUser() {
        lobby.addUser(new User());
        lobby.addUser(new User("User"));
        lobby.addUser(new User("User2", false));

        assertNotNull(lobby.getUser(1));
        assertEquals("User", lobby.getUser(2).getName());
        assertFalse(lobby.getUser(3).isReady());
    }

    @Test
    public void setUser() {
        lobby.setUser(0, new User("User"));
        assertEquals("User", lobby.getUser(0).getName());
    }

    @Test
    public void getLobbyID() {
        assertEquals(1, lobby.getLobbyID());
    }
}