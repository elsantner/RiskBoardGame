package edu.aau.se2.server.networking.dto;

import java.util.ArrayList;

public class InitialArmyPlacingMessage extends InLobbyMessage {
    private ArrayList<Integer> playerOrder;

    public InitialArmyPlacingMessage() {
        super();
    }

    public InitialArmyPlacingMessage(int lobbyID, int fromPlayerID, ArrayList<Integer> playerOrder) {
        super(lobbyID, fromPlayerID);
        this.playerOrder = playerOrder;
    }

    public ArrayList<Integer> getPlayerOrder() {
        return playerOrder;
    }

    public void setPlayerOrder(ArrayList<Integer> playerOrder) {
        this.playerOrder = playerOrder;
    }
}
