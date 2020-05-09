package edu.aau.se2.server.networking.dto.game;

import edu.aau.se2.server.networking.dto.InLobbyMessage;

public class AttackStartedMessage extends InLobbyMessage {
    private int fromTerritoryID;
    private int onTerritoryID;
    private int count;

    public AttackStartedMessage() {
        super();
    }

    public AttackStartedMessage(int lobbyID, int fromPlayerID, int fromTerritoryID, int onTerritoryID, int count) {
        super(lobbyID, fromPlayerID);
        this.fromTerritoryID = fromTerritoryID;
        this.onTerritoryID = onTerritoryID;
        this.count = count;
    }

    public int getFromTerritoryID() {
        return fromTerritoryID;
    }

    public void setFromTerritoryID(int fromTerritoryID) {
        this.fromTerritoryID = fromTerritoryID;
    }

    public int getOnTerritoryID() {
        return onTerritoryID;
    }

    public void setOnTerritoryID(int onTerritoryID) {
        this.onTerritoryID = onTerritoryID;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
