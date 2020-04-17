package edu.aau.se2.server.networking.dto;

public class ArmyPlacedMessage extends InLobbyMessage {

    private int onTerritoryID;
    private int armyCountPlaced;
    private int armyCountRemaining;

    public ArmyPlacedMessage() {
        super();
    }

    public ArmyPlacedMessage(int lobbyID, int fromPlayerID, int onTerritoryID, int armyCountPlaced) {
        super(lobbyID, fromPlayerID);
        this.onTerritoryID = onTerritoryID;
        this.armyCountPlaced = armyCountPlaced;
    }

    public int getOnTerritoryID() {
        return onTerritoryID;
    }

    public void setOnTerritoryID(int onTerritoryID) {
        this.onTerritoryID = onTerritoryID;
    }

    public int getArmyCountPlaced() {
        return armyCountPlaced;
    }

    public void setArmyCountPlaced(int armyCountPlaced) {
        this.armyCountPlaced = armyCountPlaced;
    }

    public int getArmyCountRemaining() {
        return armyCountRemaining;
    }

    public void setArmyCountRemaining(int armyCountRemaining) {
        this.armyCountRemaining = armyCountRemaining;
    }

    @Override
    public String toString() {
        return "ArmyPlacedMessage{" +
                "onTerritoryID=" + onTerritoryID +
                ", armyCountPlaced=" + armyCountPlaced +
                ", armyCountRemaining=" + armyCountRemaining +
                ", lobbyID=" + lobbyID +
                ", fromPlayerID=" + fromPlayerID +
                '}';
    }
}