package edu.aau.se2.server.networking.dto;

public class NewArmiesMessage extends InLobbyMessage {
    private int newArmyCount;
    // TODO: Add card information once cards are implemented


    public NewArmiesMessage() {
        super();
    }

    public NewArmiesMessage(int lobbyID, int fromPlayerID, int newArmyCount) {
        super(lobbyID, fromPlayerID);
        this.newArmyCount = newArmyCount;
    }

    public int getNewArmyCount() {
        return newArmyCount;
    }

    public void setNewArmyCount(int newArmyCount) {
        this.newArmyCount = newArmyCount;
    }
}
