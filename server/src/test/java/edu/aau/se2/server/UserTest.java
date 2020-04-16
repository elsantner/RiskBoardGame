package edu.aau.se2.server;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserTest {

   private User user;

    @After
    public void tearDown() throws Exception {
      this.user = null;
    }

    @Test
    public void testConstructor1() {
        user = new User("User");
        assertEquals("User", user.getName());
        assertFalse(user.isReady());
        assertFalse(user.isHost());
    }

    @Test
    public void testConstructor2() {
        user = new User("User", true);
        assertEquals("User", user.getName());
        assertTrue(user.isReady());
        assertFalse(user.isHost());
    }
}