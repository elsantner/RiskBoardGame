package edu.aau.se2.server.networking.dto.game;

import edu.aau.se2.server.networking.dto.InLobbyMessage;

public class OccupyTerritoryMessage extends InLobbyMessage {
    private int territoryID;
    private int fromTerritoryID;
    private int armyCount;

    public OccupyTerritoryMessage() {
    }

    public OccupyTerritoryMessage(int lobbyID, int fromPlayerID, int territoryID, int fromTerritoryID, int armyCount) {
        super(lobbyID, fromPlayerID);
        this.territoryID = territoryID;
        this.fromTerritoryID = fromTerritoryID;
        this.armyCount = armyCount;
    }

    public int getTerritoryID() {
        return territoryID;
    }

    public void setTerritoryID(int territoryID) {
        this.territoryID = territoryID;
    }

    public int getFromTerritoryID() {
        return fromTerritoryID;
    }

    public void setFromTerritoryID(int fromTerritoryID) {
        this.fromTerritoryID = fromTerritoryID;
    }

    public int getArmyCount() {
        return armyCount;
    }

    public void setArmyCount(int armyCount) {
        this.armyCount = armyCount;
    }
}
