package edu.aau.se2.server.data;

public class Territory {
    private int id;
    private int armyCount;
    private int occupierPlayerID;

    public Territory(int id) {
        this.id = id;
        this.armyCount = 0;
        this.occupierPlayerID = -1;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getArmyCount() {
        return armyCount;
    }

    public void setArmyCount(int armyCount) {
        this.armyCount = armyCount;
    }

    public void addToArmyCount(int armyCountSummand) {
        this.armyCount += armyCountSummand;
    }

    public int getOccupierPlayerID() {
        return occupierPlayerID;
    }

    public void setOccupierPlayerID(int occupierPlayerID) {
        this.occupierPlayerID = occupierPlayerID;
    }

    public boolean isNotOccupied() {
        return armyCount == 0;
    }
}