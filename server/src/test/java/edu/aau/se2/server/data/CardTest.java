package edu.aau.se2.server.data;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static edu.aau.se2.server.data.Card.CARD_TYPE.WILDCARD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CardTest {

    private Card card;

    @Before
    public void setUp() {
        card = new Card("test", Card.CARD_TYPE.CAVALRY, Card.CONTINENT.EUROPE,0);
    }

    @After
    public void tearDown() {
        card = null;
    }

    @Test
    public void testConstructor() {
        Card c = new Card("test1", Card.CARD_TYPE.CAVALRY, Card.CONTINENT.EUROPE, 0);
        assertEquals("test1", c.getCardName());
        assertEquals(Card.CARD_TYPE.CAVALRY, c.getCardType());
        assertEquals(Card.CONTINENT.EUROPE, c.getContinent());
        assertEquals(-1, c.getOwnerID());
    }

    @Test
    public void testGetCardName() {
        assertEquals("test", card.getCardName());
    }

    @Test
    public void testGetCard_type() {
        assertEquals(Card.CARD_TYPE.CAVALRY, card.getCardType());
    }

    @Test
    public void testGetContinent() {
        assertEquals(Card.CONTINENT.EUROPE, card.getContinent());
    }

    @Test
    public void testGetOwnerID() {
        assertEquals(-1, card.getOwnerID());
    }

    @Test
    public void testSetOwnerID() {
        card.setOwnerID(5);
        assertEquals(5, card.getOwnerID());
    }

    @Test
    public void testWildCard() {
        Card c = new Card("card_wild1", WILDCARD, null,0);
        assertEquals(WILDCARD, c.getCardType());
        assertNull(c.getContinent());
    }
}
