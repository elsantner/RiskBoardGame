package edu.aau.se2.server.networking.dto;

import java.util.List;

public class InitialArmyPlacingMessage extends InLobbyMessage {
    private List<Integer> playerOrder;

    public InitialArmyPlacingMessage() {
        super();
    }

    public InitialArmyPlacingMessage(int lobbyID, int fromPlayerID, List<Integer> playerOrder) {
        super(lobbyID, fromPlayerID);
        this.playerOrder = playerOrder;
    }

    public List<Integer> getPlayerOrder() {
        return playerOrder;
    }

    public void setPlayerOrder(List<Integer> playerOrder) {
        this.playerOrder = playerOrder;
    }
}
