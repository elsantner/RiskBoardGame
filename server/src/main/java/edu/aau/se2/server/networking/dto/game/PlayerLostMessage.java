package edu.aau.se2.server.networking.dto.game;

import edu.aau.se2.server.networking.dto.InLobbyMessage;

public class PlayerLostMessage extends InLobbyMessage {

    public PlayerLostMessage() {
    }

    public PlayerLostMessage(int lobbyID, int fromPlayerID) {
        super(lobbyID, fromPlayerID);
    }
}
