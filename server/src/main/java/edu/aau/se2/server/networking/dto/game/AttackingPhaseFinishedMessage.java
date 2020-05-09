package edu.aau.se2.server.networking.dto.game;

import edu.aau.se2.server.networking.dto.InLobbyMessage;

public class AttackingPhaseFinishedMessage extends InLobbyMessage {
    public AttackingPhaseFinishedMessage() {
    }

    public AttackingPhaseFinishedMessage(int lobbyID, int fromPlayerID) {
        super(lobbyID, fromPlayerID);
    }
}
