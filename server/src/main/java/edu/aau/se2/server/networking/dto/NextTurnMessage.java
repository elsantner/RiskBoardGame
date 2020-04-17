package edu.aau.se2.server.networking.dto;

public class NextTurnMessage extends InLobbyMessage {
    private int playerToActID;

    public NextTurnMessage() {
        super();
    }

    public NextTurnMessage(int lobbyID, int fromPlayerID) {
        this(lobbyID, fromPlayerID, -1);
    }

    public NextTurnMessage(int lobbyID, int fromPlayerID, int playerToActID) {
        super(lobbyID, fromPlayerID);
        this.playerToActID = playerToActID;
    }

    public int getPlayerToActID() {
        return playerToActID;
    }

    public void setPlayerToActID(int playerToActID) {
        this.playerToActID = playerToActID;
    }
}
