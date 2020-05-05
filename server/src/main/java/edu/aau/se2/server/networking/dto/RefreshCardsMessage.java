package edu.aau.se2.server.networking.dto;

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
