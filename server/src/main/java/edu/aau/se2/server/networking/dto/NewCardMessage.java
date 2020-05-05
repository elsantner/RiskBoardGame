package edu.aau.se2.server.networking.dto;

public class NewCardMessage extends InLobbyMessage {
    private String cardName;
    private boolean askForCardExchange;

    public NewCardMessage(){
        super();
    }

    public NewCardMessage(int lobbyID, int fromPlayerID, String cardName, boolean askForCardExchange) {
        super(lobbyID, fromPlayerID);
        this.cardName = cardName;
        this.askForCardExchange = askForCardExchange;
    }

    public String getCardName() {
        return cardName;
    }

    public boolean isAskForCardExchange() {
        return askForCardExchange;
    }
}
