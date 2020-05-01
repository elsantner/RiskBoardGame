package edu.aau.se2.server.networking.dto;

public class NewCardMessage extends InLobbyMessage {
    private String cardName;

    public NewCardMessage(){
        super();
    }

    public NewCardMessage(int lobbyID, int fromPlayerID, String cardName) {
        super(lobbyID, fromPlayerID);
        this.cardName = cardName;
    }

    public String getCardName() {
        return cardName;
    }
}
