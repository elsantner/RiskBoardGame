package edu.aau.se2.server.data;

import java.util.ArrayList;
import java.util.Random;

import static edu.aau.se2.server.data.Card.CARD_TYPE.*;
import static edu.aau.se2.server.data.Card.CONTINENT.*;

public class CardDeck {
    private int lobbyID;
    private Card[] deck;

    public CardDeck(int id) {
        this.lobbyID = id;
        deck = new Card[]{
                new Card("card_wild1", WILDCARD, null),
                new Card("card_wild2", WILDCARD, null),
                new Card("card_afghanistan", INFANTRY, ASIA),
                new Card("card_alaska", INFANTRY, NORTH_AMERICA),
                new Card("card_argentina", INFANTRY, SOUTH_AMERICA),
                new Card("card_brazil", ARTILLERY, SOUTH_AMERICA),
                new Card("card_central_africa", CAVALRY, AFRICA),
                new Card("card_central_america", CAVALRY, NORTH_AMERICA),
                new Card("card_central_canada", CAVALRY, NORTH_AMERICA),
                new Card("card_central_europe", CAVALRY, EUROPE),
                new Card("card_china", CAVALRY, ASIA),
                new Card("card_east_africa", ARTILLERY, AFRICA),
                new Card("card_eastern_australia", INFANTRY, AUSTRALIA),
                new Card("card_eastern_canada", ARTILLERY, NORTH_AMERICA),
                new Card("card_eastern_us", ARTILLERY, NORTH_AMERICA),
                new Card("card_egypt", INFANTRY, AFRICA),
                new Card("card_great_britain", CAVALRY, EUROPE),
                new Card("card_greenland", CAVALRY, NORTH_AMERICA),
                new Card("card_iceland", INFANTRY, EUROPE),
                new Card("card_india", INFANTRY, ASIA),
                new Card("card_indonesia", CAVALRY, AUSTRALIA),
                new Card("card_irkutsk", INFANTRY, ASIA),
                new Card("card_japan", INFANTRY, ASIA),
                new Card("card_kamchatka", CAVALRY, ASIA),
                new Card("card_madagascar", INFANTRY, AFRICA),
                new Card("card_middle_east", ARTILLERY, ASIA),
                new Card("card_mongolia", ARTILLERY, ASIA),
                new Card("card_new_guinea", CAVALRY, AUSTRALIA),
                new Card("card_north_africa", INFANTRY, AFRICA),
                new Card("card_northwest_territory", ARTILLERY, NORTH_AMERICA),
                new Card("card_peru", CAVALRY, SOUTH_AMERICA),
                new Card("card_scandinavia", ARTILLERY, EUROPE),
                new Card("card_siberia", ARTILLERY, ASIA),
                new Card("card_south_africa", ARTILLERY, AFRICA),
                new Card("card_southeast_asia", ARTILLERY, ASIA),
                new Card("card_southern_europe", CAVALRY, EUROPE),
                new Card("card_ukraine", ARTILLERY, EUROPE),
                new Card("card_ural", CAVALRY, ASIA),
                new Card("card_venezuela", ARTILLERY, SOUTH_AMERICA),
                new Card("card_western_australia", ARTILLERY, AUSTRALIA),
                new Card("card_western_canada", INFANTRY, NORTH_AMERICA),
                new Card("card_western_europe", INFANTRY, EUROPE),
                new Card("card_western_us", INFANTRY, NORTH_AMERICA),
                new Card("card_yakutsk", CAVALRY, ASIA),
        };
    }

    public int getLobbyID() {
        return lobbyID;
    }

    public Card[] getDeck() {
        return deck;
    }

    public Card getCard(String cardName) {
        for (Card c : deck
        ) {
            if (c.getCardName().equals(cardName)) {
                return c;
            }
        }
        return null;
    }

    private Card[] getUnassignedCards() {
        ArrayList<Card> cards = new ArrayList<>();
        for (Card c : deck
        ) {
            if (c.getOwnerID() == -1) cards.add(c);
        }
        return cards.toArray(new Card[0]);
    }

    public Card getRandomCard(int ownerID) {
        Card[] cards = getUnassignedCards();
        if (cards.length == 0) return null; //no cards left

        int random = new Random().nextInt(cards.length);
        cards[random].setOwnerID(ownerID);

        return cards[random];
    }
}
