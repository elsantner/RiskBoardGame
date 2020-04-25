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
        card = new Card("test", Card.CARD_TYPE.CAVALRY, Card.CONTINENT.EUROPE);
    }

    @After
    public void tearDown() {
        card = null;
    }

    @Test
    public void testConstructor() {
        Card c = new Card("test1", Card.CARD_TYPE.CAVALRY, Card.CONTINENT.EUROPE);
        assertEquals("test1", c.getCardName());
        assertEquals(Card.CARD_TYPE.CAVALRY, c.getCard_type());
        assertEquals(Card.CONTINENT.EUROPE, c.getContinent());
        assertEquals(-1, c.getOwnerID());
    }

    @Test
    public void testGetCardName() {
        assertEquals("test", card.getCardName());
    }

    @Test
    public void testGetCard_type() {
        assertEquals(Card.CARD_TYPE.CAVALRY, card.getCard_type());
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
        Card c = new Card("card_wild1", WILDCARD, null);
        assertEquals(WILDCARD, c.getCard_type());
        assertNull(c.getContinent());
    }
}
