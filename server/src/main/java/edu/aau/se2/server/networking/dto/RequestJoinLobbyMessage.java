package edu.aau.se2.server.networking.dto;

public class RequestJoinLobbyMessage extends InLobbyMessage {

    public RequestJoinLobbyMessage() {
    }

    public RequestJoinLobbyMessage(int lobbyID, int fromPlayerID) {
        super(lobbyID, fromPlayerID);
    }
}
