package edu.aau.se2.server.networking.dto.game;

import edu.aau.se2.server.networking.dto.InLobbyMessage;

public class ArmyMovedMessage extends InLobbyMessage {

    private int fromTerritoryID;
    private int toTerritoryID;
    private int armyCountMoved;

    public ArmyMovedMessage() {
    }

    public ArmyMovedMessage(int lobbyID, int fromPlayerID, int fromTerritoryID, int toTerritoryID, int armyCountMoved) {
        super(lobbyID, fromPlayerID);
    }

    public int getFromTerritoryID() {
        return fromTerritoryID;
    }

    public void setFromTerritoryID(int fromTerritoryID) {
        this.fromTerritoryID = fromTerritoryID;
    }

    public int getToTerritoryID() {
        return toTerritoryID;
    }

    public void setToTerritoryID(int toTerritoryID) {
        this.toTerritoryID = toTerritoryID;
    }

    public int getArmyCountMoved() {
        return armyCountMoved;
    }

    public void setArmyCountMoved(int armyCountMoved) {
        this.armyCountMoved = armyCountMoved;
    }
}
