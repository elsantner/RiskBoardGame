package edu.aau.se2.server.networking.dto;

public class RequestLeaveLobby extends InLobbyMessage {
    public RequestLeaveLobby() {
    }

    public RequestLeaveLobby(int lobbyID, int fromPlayerID) {
        super(lobbyID, fromPlayerID);
    }
}
