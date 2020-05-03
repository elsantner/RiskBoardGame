package edu.aau.se2.server.data;

public class Card {
    public enum CARD_TYPE {INFANTRY, CAVALRY, ARTILLERY, WILDCARD}
    public enum CONTINENT {ASIA, NORTH_AMERICA, EUROPE, AFRICA, SOUTH_AMERICA, AUSTRALIA}

    private String cardName;
    private CARD_TYPE cardType;
    private CONTINENT continent;
    private int ownerID;  // owner = playerID || -1 : no owner yet || -2 : traded in cards
    private int cardID; // cardID is based on TerritoryHelper's cardIDs (wildcards have -1,-2)

    public Card(String cardName, CARD_TYPE cardType, CONTINENT continent, int cardID) {
        this.cardName = cardName;
        this.cardType = cardType;
        this.continent = continent;
        this.ownerID = -1;
        this.cardID = cardID;
    }

    public String getCardName() {
        return cardName;
    }

    public CARD_TYPE getCardType() {
        return cardType;
    }

    public CONTINENT getContinent() {
        return continent;
    }

    public int getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(int ownerID) {
        this.ownerID = ownerID;
    }

    public int getCardID() {
        return cardID;
    }
}
