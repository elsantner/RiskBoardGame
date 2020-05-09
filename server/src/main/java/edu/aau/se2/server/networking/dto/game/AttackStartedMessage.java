package edu.aau.se2.server.networking.dto.game;

import edu.aau.se2.server.networking.dto.InLobbyMessage;

public class AttackStartedMessage extends InLobbyMessage {
    private int fromTerritoryID;
    private int toTerritoryID;
    private int diceCount;

    public AttackStartedMessage() {

    }

    public AttackStartedMessage(int lobbyID, int fromPlayerID, int fromTerritoryID, int toTerritoryID, int diceCount) {
        super(lobbyID, fromPlayerID);
        this.fromTerritoryID = fromTerritoryID;
        this.toTerritoryID = toTerritoryID;
        this.diceCount = diceCount;
    }

    public int getFromTerritoryID() {
        return fromTerritoryID;
    }

    public int getToTerritoryID() {
        return toTerritoryID;
    }

    public int getDiceCount() {
        return diceCount;
    }

    public void setDiceCount(int diceCount) {
        this.diceCount = diceCount;
    }
}
