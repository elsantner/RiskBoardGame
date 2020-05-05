package edu.aau.se2.server.networking.dto;

public class CardExchangeMessage extends InLobbyMessage {

    private boolean exchangeSet;

    public CardExchangeMessage() {
        super();
    }

    public CardExchangeMessage(int lobbyID, int fromPlayerID, boolean exchangeSet) {
        super(lobbyID, fromPlayerID);
        this.exchangeSet = exchangeSet;
    }

    public boolean isExchangeSet() {
        return exchangeSet;
    }
}
