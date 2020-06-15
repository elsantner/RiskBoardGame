package edu.aau.se2.server.logic;

import java.util.ArrayList;
import java.util.List;

import edu.aau.se2.server.data.Territory;

public abstract class ArmyCountHelper {

    private ArmyCountHelper() {
        // defeat instantiation
    }

    private static Integer[] continentBonusArmies = new Integer[] {2, 5, 3, 5, 2, 7};

    public static int getStartCount(int playerCount) {
        switch (playerCount) {
            case 2:
                return 3;
            case 3:
                return 3;
            case 4:
                return 3;
            case 5:
                return 3;
            case 6:
                return 3;
            default:
                throw new IllegalArgumentException("initial army count is only defined for 2-6 players");
        }
    }

    /**
     * Calculates the number of new armies the player with playerID would get at the beginning of
     * a new turn with the given territories.
     * @param territories Current configuration of all 42 territories
     * @param playerID PlayerID to calculate for.
     * @return Number of new armies the player would receive at the start of a new turn.
     */
    public static int getNewArmyCount(Territory[] territories, int playerID) {
        int armyCount;
        List<Integer> occupiedTerritoryIDs = getTerritoryIDsOfPlayer(territories, playerID);

        // calculate according to official game rules
        armyCount = occupiedTerritoryIDs.size() / 3;
        for (int continentID: TerritoryHelper.getFullyIncludedContinents(occupiedTerritoryIDs)) {
            armyCount += continentBonusArmies[continentID];
        }
        // minimum number of new armies is 3
        if (armyCount < 3) {
            armyCount = 3;
        }
        return armyCount;
    }

    private static List<Integer> getTerritoryIDsOfPlayer(Territory[] territories, int playerID) {
        List<Integer> occupiedTerritoryIDs = new ArrayList<>();
        for (Territory t: territories) {
            if (t.getOccupierPlayerID() == playerID) {
                occupiedTerritoryIDs.add(t.getId());
            }
        }
        return occupiedTerritoryIDs;
    }
}
