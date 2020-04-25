package edu.aau.se2.server.data;

public class Card {
    public enum CARD_TYPE  {INFANTRY, CAVALRY, ARTILLERY, WILDCARD}
    public enum CONTINENT {ASIA, NORTH_AMERICA, EUROPE, AFRICA, SOUTH_AMERICA, AUSTRALIA}

    private String cardName;
    private CARD_TYPE card_type;
    private CONTINENT continent;
    private int ownerID;


    public Card(String cardName, CARD_TYPE card_type, CONTINENT continent) {
        this.cardName = cardName;
        this.card_type = card_type;
        this.continent = continent;
        this.ownerID = -1;
    }

    public String getCardName() {
        return cardName;
    }

    public CARD_TYPE getCard_type() {
        return card_type;
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
}
