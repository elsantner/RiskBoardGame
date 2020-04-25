package edu.aau.se2.server.data;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class CardDeckTest {

    private CardDeck cardDeck;

    @Before
    public void setUp() {
        cardDeck = new CardDeck(1);
    }

    @After
    public void tearDown() {
        cardDeck = null;
    }

    @Test
    public void testConstructor() {
        CardDeck testDeck = new CardDeck(1);
        assertEquals(1, testDeck.getLobbyID());
        assertNotNull(testDeck.getDeck());
    }

    @Test
    public void testGetLobbyID() {
        assertEquals(1, cardDeck.getLobbyID());
    }

    @Test
    public void testGetDeck() {
        assertNotNull(cardDeck.getDeck());
    }

    @Test
    public void testGetCard() {
        Card c = cardDeck.getCard("card_iceland");
        assertEquals("card_iceland", c.getCardName());
        assertEquals(Card.CONTINENT.EUROPE, c.getContinent());
        assertEquals(Card.CARD_TYPE.INFANTRY, c.getCard_type());
    }

    @Test
    public void testGetCardNotInDeck() {
        assertNull(cardDeck.getCard("not here"));
    }

    @Test
    public void testGetRandomCardSimple() {
        Card c = cardDeck.getRandomCard(1);
        assertNotNull(c);
        assertEquals(1, c.getOwnerID());
    }

    @Test
    public void testGetRandomCardAdvanced() {
        int size = cardDeck.getDeck().length;
        Card[] cards = new Card[size];

        // get all cards from deck in random order
        for (int i = 0; i < size; i++) {
            cards[i] = cardDeck.getRandomCard(i);
        }

        // Every returned has the correct id?
        for (int i = 0; i < cards.length; i++) {
            if (cards[i].getOwnerID() != i) fail();
        }

        // Prints all cards to check if really random
        for (Card c : cards
        ) {
            System.out.println(+c.getOwnerID() + "\t | " + c.getCardName());
        }

        // check if every card is only returned once
        Card[] deck = cardDeck.getDeck();
        for (Card card : cards) {
            for (Card c : deck
            ) {
                if (c.getCardName().equals(card.getCardName())) {
                    if (card.getOwnerID() == -5) fail();
                    card.setOwnerID(-5);
                }
            }
        }
    }

    @Test
    public void testGetRandomCardToMany() {
        int size = cardDeck.getDeck().length;
        for (int i = 0; i < size; i++) {
            cardDeck.getRandomCard(1);
        }
        assertNull(cardDeck.getRandomCard(1));

    }
}
