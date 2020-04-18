package edu.aau.se2.server.logic;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import edu.aau.se2.server.data.Territory;

public class ArmyCountHelperTest {

    private Territory[] territories;

    @Before
    public void setup() {
        this.territories = new Territory[42];
        for (int i=0; i<42; i++) {
            this.territories[i] = new Territory(i+1);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInitialCountException0() {
        ArmyCountHelper.getStartCount(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInitialCountException7() {
        ArmyCountHelper.getStartCount(7);
    }

    @Test
    public void testCalcNewArmyCount() {
        int playerID = 1;

        // test minimum amount
        Assert.assertEquals(3, ArmyCountHelper.getNewArmyCount(territories, playerID));

        territories[TerritoryHelper.ID.INDONESIA].setOccupierPlayerID(playerID);
        territories[TerritoryHelper.ID.NEW_GUINEA].setOccupierPlayerID(playerID);
        territories[TerritoryHelper.ID.EASTERN_AUSTRALIA].setOccupierPlayerID(playerID);
        territories[TerritoryHelper.ID.WESTERN_AUSTRALIA].setOccupierPlayerID(playerID);
        territories[TerritoryHelper.ID.EASTERN_US].setOccupierPlayerID(playerID);
        territories[TerritoryHelper.ID.WESTERN_EUROPE].setOccupierPlayerID(playerID);
        territories[TerritoryHelper.ID.SIBERIA].setOccupierPlayerID(playerID);
        territories[TerritoryHelper.ID.YAKUTSK].setOccupierPlayerID(playerID);
        territories[TerritoryHelper.ID.URAL].setOccupierPlayerID(playerID);
        territories[TerritoryHelper.ID.EGYPT].setOccupierPlayerID(playerID);
        territories[TerritoryHelper.ID.JAPAN].setOccupierPlayerID(playerID);
        territories[TerritoryHelper.ID.ALASKA].setOccupierPlayerID(playerID);

        // 12 Territories occupied (incl. all of Australia) --> 12/3 + 2 (for Australia)
        Assert.assertEquals(4+2, ArmyCountHelper.getNewArmyCount(territories, playerID));
    }
}
