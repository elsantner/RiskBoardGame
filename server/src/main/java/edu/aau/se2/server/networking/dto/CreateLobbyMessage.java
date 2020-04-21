package edu.aau.se2.server.networking.dto;

public class CreateLobbyMessage extends BaseMessage {
    private int playerID;

    public CreateLobbyMessage() {
        super();
    }

    public CreateLobbyMessage(int playerID) {
        this.playerID = playerID;
    }

    public int getPlayerID() {
        return playerID;
    }

    public void setPlayerID(int playerID) {
        this.playerID = playerID;
    }
}
