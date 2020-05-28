package edu.aau.se2.server.data;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class LobbyTest {

    private DataStoreTestClass ds;
    private Lobby lobby;

    @Before
    public void setUp() {
        ds = new DataStoreTestClass();
        lobby = ds.createLobby(new Player(0, "host"));
    }

    @After
    public void tearDown() {
        this.ds = null;
    }

    @Test
    public void testConstructor() {
        assertNotNull(lobby.getPlayers());
        assertFalse(lobby.isStarted());
        assertFalse(lobby.allTerritoriesOccupied());
        assertFalse(lobby.canStartGame());
    }

    @Test
    public void addUser() {
        assertTrue(lobby.removePlayer(0));
        assertFalse(lobby.removePlayer(-1));

        lobby.setHost(new Player(0, "User0"));
        lobby.addPlayer(new Player(1, "User1"));
        lobby.addPlayer(new Player(2, "User2"));
        assertEquals(2, lobby.getPlayerByID(2).getUid());

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

    @Test(expected = IllegalArgumentException.class)
    public void testUpdatePlayer() {
        lobby.updatePlayer(new Player(0, "UpdatedNickname"));
        assertEquals("UpdatedNickname", lobby.getPlayerByID(0).getNickname());
        // must throw exception
        lobby.updatePlayer(new Player(2, "NewPlayer"));
    }

    @Test
    public void testGetTerritories() {
        assertNull(lobby.getPlayerByTerritoryID(0));
        Territory[] unoccupiedTerritories = lobby.getUnoccupiedTerritories();
        assertEquals(42, unoccupiedTerritories.length);

        lobby.getTerritoryByID(0).setOccupierPlayerID(0);
        assertEquals(0, lobby.getPlayerByTerritoryID(0).getUid());
        assertEquals(41, lobby.getUnoccupiedTerritories().length);
    }

    @Test
    public void testIsPlayersTurn() {
        lobby.join(new Player(1, "P1"));
        // no turn order set yet
        assertFalse(lobby.isPlayersTurn(1));
        lobby.setTurnOrder(Arrays.asList(1, 0));
        assertFalse(lobby.isPlayersTurn(0));
        assertTrue(lobby.isPlayersTurn(1));
    }

    @Test
    public void testClearPlayers() {
        lobby.join(new Player(1, "P1"));
        assertEquals(2, lobby.getPlayers().size());
        lobby.clearPlayers();
        assertEquals(0, lobby.getPlayers().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetReadyIllegalPlayer() {
        lobby.setPlayerReady(-1, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetIllegalTerritory() {
        lobby.getTerritoryByID(-1);
    }

    @Test(expected = IllegalStateException.class)
    public void testJoinStartedLobby() {
        lobby.setStarted(true);
        lobby.join(new Player(1, "P1"));
    }
}