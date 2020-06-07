package edu.aau.se2.server.networking.dto.game;

import edu.aau.se2.server.networking.dto.InLobbyMessage;

public class VictoryMessage extends InLobbyMessage {
    public VictoryMessage(int lobbyID, int fromPlayerID) {
        super(lobbyID, fromPlayerID);
    }

    public VictoryMessage() {
    }

}
