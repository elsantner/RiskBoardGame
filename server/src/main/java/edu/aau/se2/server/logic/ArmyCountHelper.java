package edu.aau.se2.server.logic;

public interface ArmyCountHelper {
    static int getStartCount(int playerCount) {
        switch (playerCount) {
            case 2:
                return 50;
            case 3:
                return 35;
            case 4:
                return 30;
            case 5:
                return 25;
            case 6:
                return 20;
            default:
                throw new IllegalArgumentException("initial army count is only defined for 2-6 players");
        }
    }
}
