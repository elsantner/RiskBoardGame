package edu.aau.se2.server.logic;

import org.junit.Test;

public class ArmyCountHelperTest {
    @Test(expected = IllegalArgumentException.class)
    public void testInitialCountException0() {
        ArmyCountHelper.getStartCount(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInitialCountException7() {
        ArmyCountHelper.getStartCount(7);
    }
}
