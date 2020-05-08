package edu.aau.se2.server.networking.dto.game;

import edu.aau.se2.server.networking.dto.InLobbyMessage;

public class CardExchangeMessage extends InLobbyMessage {
    // TODO: Add card information once cards are implemented

    public CardExchangeMessage() {
        super();
    }

    public CardExchangeMessage(int lobbyID, int fromPlayerID) {
        super(lobbyID, fromPlayerID);
    }
}
