package edu.aau.se2.server.networking.dto.game;

import edu.aau.se2.server.networking.dto.InLobbyMessage;

public class RefreshCardsMessage extends InLobbyMessage {
    private String[] cardNames;

    public RefreshCardsMessage() {
        super();
    }

    public RefreshCardsMessage(int lobbyID, int fromPlayerID, String[] cardNames) {
        super(lobbyID, fromPlayerID);
        this.cardNames = cardNames;
    }

    public String[] getCardNames() {
        return cardNames;
    }
}
