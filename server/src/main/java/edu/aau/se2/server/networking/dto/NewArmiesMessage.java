package edu.aau.se2.server.networking.dto;

public class NewArmiesMessage extends InLobbyMessage {
    private int newArmyCount;
    private int territoryIdForBonusArmies = -1;


    public NewArmiesMessage() {
        super();
    }

    public NewArmiesMessage(int lobbyID, int fromPlayerID, int newArmyCount, int territoryIdForBonusArmies) {
        super(lobbyID, fromPlayerID);
        this.newArmyCount = newArmyCount;
        this.territoryIdForBonusArmies = territoryIdForBonusArmies;
    }

    public int getNewArmyCount() {
        return newArmyCount;
    }

    public void setNewArmyCount(int newArmyCount) {
        this.newArmyCount = newArmyCount;
    }

    public int getTerritoryIdForBonusArmies() {
        return territoryIdForBonusArmies;
    }
}
