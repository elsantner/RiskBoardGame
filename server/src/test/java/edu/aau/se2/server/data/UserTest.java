package edu.aau.se2.server.data;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserTest {

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
}