package edu.aau.se2.server.networking.dto.game;

import edu.aau.se2.server.networking.dto.InLobbyMessage;

public class LeftGameMessage extends InLobbyMessage {
    private boolean hasLost = false;

    public LeftGameMessage() {
    }

    public LeftGameMessage(int lobbyID, int fromPlayerID) {
        super(lobbyID, fromPlayerID);
    }

    public LeftGameMessage(int lobbyID, int fromPlayerID, boolean hasLost) {
        super(lobbyID, fromPlayerID);
        this.hasLost = hasLost;
    }

    public boolean isHasLost() {
        return hasLost;
    }
}
