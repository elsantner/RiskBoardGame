package edu.aau.se2.server.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static edu.aau.se2.server.data.Card.CARD_TYPE.*;
import static edu.aau.se2.server.data.Card.CONTINENT.*;

public class CardDeck {
    private int lobbyID;
    private int setsTradedIn;
    private Card[] deck;

    public CardDeck(int id) {
        this.lobbyID = id;
        this.setsTradedIn = 0;
        deck = new Card[]{
                new Card("card_wild1", WILDCARD, null, -1),
                new Card("card_wild2", WILDCARD, null, -2),
                new Card("card_afghanistan", INFANTRY, ASIA, 30),
                new Card("card_alaska", INFANTRY, NORTH_AMERICA, 4),
                new Card("card_argentina", INFANTRY, SOUTH_AMERICA, 0),
                new Card("card_brazil", ARTILLERY, SOUTH_AMERICA, 1),
                new Card("card_central_africa", CAVALRY, AFRICA, 13),
                new Card("card_central_america", CAVALRY, NORTH_AMERICA, 6),
                new Card("card_central_canada", CAVALRY, NORTH_AMERICA, 10),
                new Card("card_central_europe", CAVALRY, EUROPE, 21),
                new Card("card_china", CAVALRY, ASIA, 31),
                new Card("card_east_africa", ARTILLERY, AFRICA, 14),
                new Card("card_eastern_australia", INFANTRY, AUSTRALIA, 26),
                new Card("card_eastern_canada", ARTILLERY, NORTH_AMERICA, 11),
                new Card("card_eastern_us", ARTILLERY, NORTH_AMERICA, 7),
                new Card("card_egypt", INFANTRY, AFRICA, 15),
                new Card("card_great_britain", CAVALRY, EUROPE, 19),
                new Card("card_greenland", CAVALRY, NORTH_AMERICA, 8),
                new Card("card_iceland", INFANTRY, EUROPE, 20),
                new Card("card_india", INFANTRY, ASIA, 32),
                new Card("card_indonesia", CAVALRY, AUSTRALIA, 27),
                new Card("card_irkutsk", INFANTRY, ASIA, 33),
                new Card("card_japan", INFANTRY, ASIA, 34),
                new Card("card_kamchatka", CAVALRY, ASIA, 35),
                new Card("card_madagascar", INFANTRY, AFRICA, 16),
                new Card("card_middle_east", ARTILLERY, ASIA, 36),
                new Card("card_mongolia", ARTILLERY, ASIA, 37),
                new Card("card_new_guinea", CAVALRY, AUSTRALIA, 28),
                new Card("card_north_africa", INFANTRY, AFRICA, 17),
                new Card("card_northwest_territory", ARTILLERY, NORTH_AMERICA, 9),
                new Card("card_peru", CAVALRY, SOUTH_AMERICA, 2),
                new Card("card_scandinavia", ARTILLERY, EUROPE, 22),
                new Card("card_siberia", ARTILLERY, ASIA, 39),
                new Card("card_south_africa", ARTILLERY, AFRICA, 18),
                new Card("card_southeast_asia", ARTILLERY, ASIA, 38),
                new Card("card_southern_europe", CAVALRY, EUROPE, 23),
                new Card("card_ukraine", ARTILLERY, EUROPE, 24),
                new Card("card_ural", CAVALRY, ASIA, 40),
                new Card("card_venezuela", ARTILLERY, SOUTH_AMERICA, 3),
                new Card("card_western_australia", ARTILLERY, AUSTRALIA, 29),
                new Card("card_western_canada", INFANTRY, NORTH_AMERICA, 5),
                new Card("card_western_europe", INFANTRY, EUROPE, 25),
                new Card("card_western_us", INFANTRY, NORTH_AMERICA, 12),
                new Card("card_yakutsk", CAVALRY, ASIA, 41),
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

    public Card getCard(int cardID) {
        for (Card c : deck
        ) {
            if (c.getCardID() == cardID) {
                return c;
            }
        }
        return null;
    }

    public int getSetsTradedIn() {
        return setsTradedIn;
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
        if (ownerID < 0) throw new IllegalArgumentException("Not a valid Owner");

        int random = new Random().nextInt(cards.length);
        cards[random].setOwnerID(ownerID);

        return cards[random];
    }

    private Card[] getCardsOfPlayer(int playerID) {
        ArrayList<Card> cards = new ArrayList<>();
        for (Card c : deck
        ) {
            if (c.getOwnerID() == playerID) cards.add(c);
        }
        return cards.toArray(new Card[0]);
    }

    public String[] getCardNamesOfPlayer(int playerID) {
        ArrayList<String> cardNames = new ArrayList<>();
        for (Card c : deck
        ) {
            if (c.getOwnerID() == playerID) {
                cardNames.add(c.getCardName());
            }
        }
        Collections.sort(cardNames);
        return cardNames.toArray(new String[0]);
    }

    /**
     * @param playerID which players cards
     * @return returns a set of three cards, that are allowed to be exchanged
     * 3 x the same type || 1 of each type || wildcards count as any type
     * (a set may only contain 1 wildcard)
     * the method checks for possible sets, until it finds one, which it then returns, using the
     * methods getCardsOfType / getCardsOfTypes
     * The method will preferably not use a wildcard (only if no other option is left)
     */
    public Card[] getCardSet(int playerID) {
        // 0:cavalry, 1:infantry, 2:artillery, 3:wild
        int[] types = new int[4];

        Card[] cards = getCardsOfPlayer(playerID);
        for (Card c : cards
        ) {
            if (c.getCardType() == CAVALRY) types[0]++;
            if (c.getCardType() == INFANTRY) types[1]++;
            if (c.getCardType() == ARTILLERY) types[2]++;
            if (c.getCardType() == WILDCARD) types[3]++;
        }

        if (types[0] >= 3) {
            return getCardsOfType(cards, CAVALRY);
        }
        if (types[1] >= 3) {
            return getCardsOfType(cards, INFANTRY);
        }
        if (types[2] >= 3) {
            return getCardsOfType(cards, ARTILLERY);
        }
        if (types[0] >= 1 && types[1] >= 1 && types[2] >= 1) {
            return getCardsOfTypes(cards, new Card.CARD_TYPE[]{CAVALRY, INFANTRY, ARTILLERY});
        }
        if (types[3] >= 1) {
            if (types[0] >= 2) {
                return getCardsOfTypes(cards, new Card.CARD_TYPE[]{CAVALRY, WILDCARD});
            }
            if (types[1] >= 2) {
                return getCardsOfTypes(cards, new Card.CARD_TYPE[]{INFANTRY, WILDCARD});
            }
            if (types[2] >= 2) {
                return getCardsOfTypes(cards, new Card.CARD_TYPE[]{ARTILLERY, WILDCARD});
            }
            if (types[0] >= 1 && types[1] >= 1) {
                return getCardsOfTypes(cards, new Card.CARD_TYPE[]{CAVALRY, INFANTRY, WILDCARD});
            }
            if (types[0] >= 1 && types[2] >= 1) {
                return getCardsOfTypes(cards, new Card.CARD_TYPE[]{CAVALRY, ARTILLERY, WILDCARD});
            }
            if (types[1] >= 1 && types[2] >= 1) {
                return getCardsOfTypes(cards, new Card.CARD_TYPE[]{INFANTRY, ARTILLERY, WILDCARD});
            }
        }

        // no set found
        return null;
    }

    private Card[] getCardsOfType(Card[] cards, Card.CARD_TYPE type) {
        Card[] set = new Card[3];
        int i = 0;
        for (Card c : cards
        ) {
            if (c.getCardType().equals(type) && i < set.length) {
                set[i] = c;
                i++;
            }
        }


        return set;
    }

    private Card[] getCardsOfTypes(Card[] cards, Card.CARD_TYPE[] types) {
        Card[] set = new Card[3];

        if (types.length == 2) {
            int y = 0;
            for (int i = 0; i < set.length; i++) {
                for (Card c : cards
                ) {
                    if (y < 2 && c.getCardType().equals(types[y])) {
                        set[i] = c;
                        y += i;
                    }
                }
            }

        } else if (types.length == 3) {
            for (int i = 0; i < set.length; i++) {
                for (Card c : cards
                ) {
                    if (c.getCardType().equals(types[i])) {
                        set[i] = c;
                    }
                }
            }
        }

        return set;
    }


    public int tradeInSet(Card[] set) {

        if (set == null || set.length != 3)
            throw new IllegalArgumentException("This is not a set!");

        // set ownership of cards to -2
        for (int i = 0; i < set.length; i++) {
            for (Card c : deck
            ) {
                if (c.getCardID() == set[i].getCardID()) {
                    c.setOwnerID(-2);
                }
            }
        }
        return getNextArmyCount();
    }


    private int getNextArmyCount() {
        int armyCount;
        switch (setsTradedIn) {
            case (0):
                armyCount = 4;
                break;
            case (1):
                armyCount = 6;
                break;
            case (2):
                armyCount = 8;
                break;
            case (3):
                armyCount = 10;
                break;
            case (4):
                armyCount = 12;
                break;
            case (5):
                armyCount = 15;
                break;
            default: // every additional set gives +5 cards
                armyCount = 15 + ((setsTradedIn - 5) * 5);

        }
        setsTradedIn++;
        return armyCount;
    }

    public int getTerritoryIDForBonusArmies(Card[] set, Territory[] playerTerritories){

        if(set == null || playerTerritories == null){
           return -1;
        }
        // 2 armies should be added to that territory
        for (Card card : set) {
            for (Territory t : playerTerritories
            ) {
                if (t.getId() == card.getCardID()) {
                    return t.getId();
                }
            }
        }
        // nothing found, return territory with invalid id
        return -1;
    }

}
