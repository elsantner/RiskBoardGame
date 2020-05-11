package edu.aau.se2.server.data;

import java.util.List;

public class Attack {
    private int fromTerritoryID;
    private int toTerritoryID;
    private int attackerDiceCount = -1;
    private int defenderDiceCount = -1;
    private List<Integer> attackerDiceResults;
    private List<Integer> defenderDiceResults;
    private boolean cheated;
    private boolean occupyRequired;

    public Attack(int fromTerritoryID, int toTerritoryID) {
        this.fromTerritoryID = fromTerritoryID;
        this.toTerritoryID = toTerritoryID;
    }

    public Attack(int fromTerritoryID, int toTerritoryID, int attackerDiceCount) {
        this(fromTerritoryID, toTerritoryID);
        this.attackerDiceCount = attackerDiceCount;
    }

    public int getAttackerDiceCount() {
        return attackerDiceCount;
    }

    public int getDefenderDiceCount() {
        return defenderDiceCount;
    }

    public List<Integer> getAttackerDiceResults() {
        return attackerDiceResults;
    }

    public List<Integer> getDefenderDiceResults() {
        return defenderDiceResults;
    }

    public boolean isCheated() {
        return cheated;
    }

    public void setDefenderDiceCount(int defenderDiceCount) {
        this.defenderDiceCount = defenderDiceCount;
    }

    public void setDefenderDiceResults(List<Integer> defenderDiceResults) {
        this.defenderDiceResults = defenderDiceResults;
    }

    public void setAttackerDiceResults(List<Integer> attackerDiceResults) {
        this.attackerDiceResults = attackerDiceResults;
    }

    public void setCheated(boolean cheated) {
        this.cheated = cheated;
    }

    public int getFromTerritoryID() {
        return fromTerritoryID;
    }

    public int getToTerritoryID() {
        return toTerritoryID;
    }

    public void setAttackerDiceCount(int attackerDiceCount) {
        this.attackerDiceCount = attackerDiceCount;
    }

    public boolean isOccupyRequired() {
        return occupyRequired;
    }

    public void setOccupyRequired(boolean occupyRequired) {
        this.occupyRequired = occupyRequired;
    }
}
