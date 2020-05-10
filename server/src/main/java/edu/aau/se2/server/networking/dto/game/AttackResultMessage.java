package edu.aau.se2.server.networking.dto.game;

import edu.aau.se2.server.networking.dto.InLobbyMessage;

public class AttackResultMessage extends InLobbyMessage {
    private int armiesLostAttacker;
    private int armiesLostDefender;
    private boolean cheated;
    private boolean occupyRequired;

    public AttackResultMessage() {
    }

    public AttackResultMessage(int lobbyID, int fromPlayerID, int armiesLostAttacker, int armiesLostDefender, boolean cheated, boolean occupyRequired) {
        super(lobbyID, fromPlayerID);
        this.armiesLostAttacker = armiesLostAttacker;
        this.armiesLostDefender = armiesLostDefender;
        this.cheated = cheated;
        this.occupyRequired = occupyRequired;
    }

    public int getArmiesLostAttacker() {
        return armiesLostAttacker;
    }

    public void setArmiesLostAttacker(int armiesLostAttacker) {
        this.armiesLostAttacker = armiesLostAttacker;
    }

    public int getArmiesLostDefender() {
        return armiesLostDefender;
    }

    public void setArmiesLostDefender(int armiesLostDefender) {
        this.armiesLostDefender = armiesLostDefender;
    }

    public boolean isCheated() {
        return cheated;
    }

    public void setCheated(boolean cheated) {
        this.cheated = cheated;
    }

    public boolean isOccupyRequired() {
        return occupyRequired;
    }

    public void setOccupyRequired(boolean occupyRequired) {
        this.occupyRequired = occupyRequired;
    }
}
