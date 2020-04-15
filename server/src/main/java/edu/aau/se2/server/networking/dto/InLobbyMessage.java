package edu.aau.se2.server.networking.dto;

public abstract class InLobbyMessage extends BaseMessage {
    protected int lobbyID;
    protected int fromPlayerID;

    public InLobbyMessage() {
    }

    public InLobbyMessage(int lobbyID, int fromPlayerID) {
        this.lobbyID = lobbyID;
        this.fromPlayerID = fromPlayerID;
    }

    public int getLobbyID() {
        return lobbyID;
    }

    public void setLobbyID(int lobbyID) {
        this.lobbyID = lobbyID;
    }

    public int getFromPlayerID() {
        return fromPlayerID;
    }

    public void setFromPlayerID(int fromPlayerID) {
        this.fromPlayerID = fromPlayerID;
    }
}
