package edu.aau.se2.server.data;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

public class PlayerTest {

   private Player user;

    @After
    public void tearDown() {
      this.user = null;
    }

    @Test
    public void testConstructor1() {
        user = new Player(0, "User");
        assertEquals("User", user.getNickname());
        assertFalse(user.isReady());
        assertEquals(0, user.getUid());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testColor() {
        user = new Player(0, "User");
        user.setColorID(1);
        assertEquals(1, user.getColorID());
        // must throw exception
        user.setColorID(6);
    }
}