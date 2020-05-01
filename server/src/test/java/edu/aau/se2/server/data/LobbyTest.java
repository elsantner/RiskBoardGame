package edu.aau.se2.server.data;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
        assertTrue(lobby.removePlayer(0));
        assertFalse(lobby.removePlayer(-1));

        lobby.setHost(new Player(0, "User0"));
        lobby.addPlayer(new Player(1, "User1"));
        lobby.addPlayer(new Player(2, "User2"));

        assertTrue(lobby.isJoinable());
        lobby.setStarted(true);
        assertFalse(lobby.isJoinable());
        lobby.setStarted(false);

        lobby.addPlayer(new Player(3, "User3"));
        lobby.addPlayer(new Player(4, "User4"));
        lobby.addPlayer(new Player(5, "User5"));

        assertFalse(lobby.isJoinable());

        assertEquals(6, lobby.getPlayers().size());
        assertEquals(0, lobby.getHost().getUid());
    }

    @Test
    public void armiesTest() {
        int exceptionCount = 0;

        Lobby lobby = ds.createLobby(new Player(0, "host"));
        lobby.join(new Player(1, "User1"));
        lobby.join(new Player(2, "User2"));
        lobby.setupForGameStart();
        lobby.setTurnOrder(Arrays.asList(0, 1, 2));

        assertFalse(lobby.hasCurrentPlayerToActReceivedNewArmies());
        assertFalse(lobby.hasCurrentPlayerToActPlacedNewArmies());

        lobby.giveNewArmiesToPlayer(0);
        assertEquals(3, lobby.getHost().getArmyReserveCount());

        assertTrue(lobby.hasCurrentPlayerToActReceivedNewArmies());
        assertFalse(lobby.hasCurrentPlayerToActPlacedNewArmies());

        lobby.getHost().setArmyReserveCount(0);

        assertTrue(lobby.hasCurrentPlayerToActReceivedNewArmies());
        assertTrue(lobby.hasCurrentPlayerToActPlacedNewArmies());

        try { lobby.join(null); } catch (Exception ex) { exceptionCount++; }
        try { lobby.join(new Player(0, "host")); } catch (Exception ex) { exceptionCount++; }
        lobby.setStarted(true);
        try { lobby.join(new Player(0, "host")); } catch (Exception ex) { exceptionCount++; }

        assertEquals(3, exceptionCount);
    }
}