package edu.aau.se2.server.networking.dto.game;

import edu.aau.se2.server.networking.dto.InLobbyMessage;

public class DefenderDiceCountMessage extends InLobbyMessage {

    private int diceCount;


    public DefenderDiceCountMessage() {
    }

    public DefenderDiceCountMessage(int lobbyID, int fromPlayerID, int diceCount) {
        super(lobbyID, fromPlayerID);
        this.diceCount = diceCount;
    }

    public int getDiceCount() {
        return diceCount;
    }
}
