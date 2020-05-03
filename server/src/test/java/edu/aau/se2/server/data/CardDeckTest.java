package edu.aau.se2.server.data;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CardDeckTest {

    private CardDeck cardDeck;
    private Territory[] playerTerritory;

    @Before
    public void setUp() {
        cardDeck = new CardDeck(1);
        playerTerritory = new Territory[4];
        playerTerritory[0] = new Territory(41);
        playerTerritory[0].setOccupierPlayerID(1);
        playerTerritory[1] = new Territory(37);
        playerTerritory[1].setOccupierPlayerID(1);
        playerTerritory[2] = new Territory(17);
        playerTerritory[2].setOccupierPlayerID(1);
        playerTerritory[3] = new Territory(1);
        playerTerritory[3].setOccupierPlayerID(1);
    }

    @After
    public void tearDown() {
        cardDeck = null;
        playerTerritory = null;
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
        assertEquals(Card.CARD_TYPE.INFANTRY, c.getCardType());
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

    @Test(expected = IllegalArgumentException.class)
    public void testGetRandomCardInvalidOwner() {
        cardDeck.getRandomCard(-1);
    }

    @Test
    public void testGetCardSet1() {
        cardDeck.getCard(13).setOwnerID(1); //cavalry
        cardDeck.getCard(6).setOwnerID(1);  //cavalry
        cardDeck.getCard(10).setOwnerID(1); //cavalry
        cardDeck.getCard(41).setOwnerID(1); //cavalry
        cardDeck.getCard(37).setOwnerID(1); //artillery
        cardDeck.getCard(17).setOwnerID(1); //infantry

        Card[] testSet = new Card[3];
        testSet[0] = cardDeck.getCard(13);
        testSet[1] = cardDeck.getCard(6);
        testSet[2] = cardDeck.getCard(10);

        Card[] resultSet = cardDeck.getCardSet(1);


        // test if cards are different
        if (resultSet[0] == resultSet[1] || resultSet[0] == resultSet[2] || resultSet[1] == resultSet[2]) {
            fail();
        }

        // are cards in set same as expected
        for (Card card : testSet) {
            boolean contains = false;
            for (Card c : resultSet
            ) {
                if (c.equals(card)) {
                    contains = true;
                    break;
                }
            }
            assertTrue(contains);
        }
    }

    @Test
    public void testGetCardSet2() {
        cardDeck.getCard(13).setOwnerID(1); //cavalry
        cardDeck.getCard(6).setOwnerID(1);  //cavalry
        cardDeck.getCard(37).setOwnerID(1); //artillery
        cardDeck.getCard(39).setOwnerID(1); //artillery
        cardDeck.getCard(17).setOwnerID(1); //infantry
        cardDeck.getCard(-1).setOwnerID(1); //wildcard

        Card[] testSet = new Card[3];
        testSet[0] = cardDeck.getCard(6);
        testSet[1] = cardDeck.getCard(39);
        testSet[2] = cardDeck.getCard(17);

        Card[] resultSet = cardDeck.getCardSet(1);


        // test if cards are different
        if (resultSet[0] == resultSet[1] || resultSet[0] == resultSet[2] || resultSet[1] == resultSet[2]) {
            fail();
        }

        // are cards in set same as expected?
        for (Card card : testSet) {
            boolean contains = false;
            for (Card c : resultSet
            ) {
                if (c.equals(card)) {
                    contains = true;
                    break;
                }
            }
            assertTrue(contains);
        }
    }

    @Test
    public void testGetCardSet3() {
        cardDeck.getCard(13).setOwnerID(1); //cavalry
        cardDeck.getCard(6).setOwnerID(1);  //cavalry
        cardDeck.getCard(37).setOwnerID(1); //artillery
        cardDeck.getCard(39).setOwnerID(1); //artillery
        cardDeck.getCard(-1).setOwnerID(1); //wildcard

        Card[] testSet = new Card[3];
        testSet[0] = cardDeck.getCard(13);
        testSet[1] = cardDeck.getCard(6);
        testSet[2] = cardDeck.getCard(-1);

        Card[] resultSet = cardDeck.getCardSet(1);


        // test if cards are different
        if (resultSet[0] == resultSet[1] || resultSet[0] == resultSet[2] || resultSet[1] == resultSet[2]) {
            fail();
        }

        // are cards in set same as expected?
        for (Card card : testSet) {
            boolean contains = false;
            for (Card c : resultSet
            ) {
                if (c.equals(card)) {
                    contains = true;
                    break;
                }
            }
            assertTrue(contains);
        }
    }

    @Test
    public void testGetCardSet4() {
        cardDeck.getCard(13).setOwnerID(1); //cavalry
        cardDeck.getCard(37).setOwnerID(1); //artillery
        cardDeck.getCard(-1).setOwnerID(1); //wildcard

        Card[] testSet = new Card[3];
        testSet[0] = cardDeck.getCard(13);
        testSet[1] = cardDeck.getCard(37);
        testSet[2] = cardDeck.getCard(-1);

        Card[] resultSet = cardDeck.getCardSet(1);


        // test if cards are different
        if (resultSet[0] == resultSet[1] || resultSet[0] == resultSet[2] || resultSet[1] == resultSet[2]) {
            fail();
        }

        // are cards in set same as expected?
        for (Card card : testSet) {
            boolean contains = false;
            for (Card c : resultSet
            ) {
                if (c.equals(card)) {
                    contains = true;
                    break;
                }
            }
            assertTrue(contains);
        }
    }

    @Test
    public void testGetCardSet5() {
        cardDeck.getCard(13).setOwnerID(1); //cavalry
        cardDeck.getCard(6).setOwnerID(1); //cavalry
        cardDeck.getCard(-1).setOwnerID(1); //wildcard

        Card[] testSet = new Card[3];
        testSet[0] = cardDeck.getCard(13);
        testSet[1] = cardDeck.getCard(6);
        testSet[2] = cardDeck.getCard(-1);

        Card[] resultSet = cardDeck.getCardSet(1);

        // are cards in set same as expected?
        for (Card card : testSet) {
            boolean contains = false;
            for (Card c : resultSet
            ) {
                if (c.equals(card)) {
                    contains = true;
                    break;
                }
            }
            assertTrue(contains);
        }
    }

    @Test
    public void testGetCardSet6() {
        cardDeck.getCard(5).setOwnerID(1); //infantry
        cardDeck.getCard(25).setOwnerID(1); //infantry
        cardDeck.getCard(-1).setOwnerID(1); //wildcard

        Card[] testSet = new Card[3];
        testSet[0] = cardDeck.getCard(25);
        testSet[1] = cardDeck.getCard(5);
        testSet[2] = cardDeck.getCard(-1);

        Card[] resultSet = cardDeck.getCardSet(1);

        // are cards in set same as expected?
        for (Card card : testSet) {
            boolean contains = false;
            for (Card c : resultSet
            ) {
                if (c.equals(card)) {
                    contains = true;
                    break;
                }
            }
            assertTrue(contains);
        }
    }

    @Test
    public void testGetCardSet7() {
        cardDeck.getCard(6).setOwnerID(1); //cavalry
        cardDeck.getCard(25).setOwnerID(1); //infantry
        cardDeck.getCard(-1).setOwnerID(1); //wildcard

        Card[] testSet = new Card[3];
        testSet[0] = cardDeck.getCard(25);
        testSet[1] = cardDeck.getCard(6);
        testSet[2] = cardDeck.getCard(-1);

        Card[] resultSet = cardDeck.getCardSet(1);

        // are cards in set same as expected?
        for (Card card : testSet) {
            boolean contains = false;
            for (Card c : resultSet
            ) {
                if (c.equals(card)) {
                    contains = true;
                    break;
                }
            }
            assertTrue(contains);
        }
    }

    @Test
    public void testGetCardSet8() {
        cardDeck.getCard(37).setOwnerID(1); //artillery
        cardDeck.getCard(25).setOwnerID(1); //infantry
        cardDeck.getCard(-1).setOwnerID(1); //wildcard

        Card[] testSet = new Card[3];
        testSet[0] = cardDeck.getCard(25);
        testSet[1] = cardDeck.getCard(37);
        testSet[2] = cardDeck.getCard(-1);

        Card[] resultSet = cardDeck.getCardSet(1);

        // are cards in set same as expected?
        for (Card card : testSet) {
            boolean contains = false;
            for (Card c : resultSet
            ) {
                if (c.equals(card)) {
                    contains = true;
                    break;
                }
            }
            assertTrue(contains);
        }
    }

    @Test
    public void testGetCardSet9() {
        cardDeck.getCard(37).setOwnerID(1); //artillery
        cardDeck.getCard(3).setOwnerID(1); //artillery
        cardDeck.getCard(-1).setOwnerID(1); //wildcard

        Card[] testSet = new Card[3];
        testSet[0] = cardDeck.getCard(3);
        testSet[1] = cardDeck.getCard(37);
        testSet[2] = cardDeck.getCard(-1);

        Card[] resultSet = cardDeck.getCardSet(1);

        // are cards in set same as expected?
        for (Card card : testSet) {
            boolean contains = false;
            for (Card c : resultSet
            ) {
                if (c.equals(card)) {
                    contains = true;
                    break;
                }
            }
            assertTrue(contains);
        }
    }

    @Test
    public void testGetCardSetNoSetFound() {
        cardDeck.getCard(13).setOwnerID(1); //cavalry
        cardDeck.getCard(17).setOwnerID(1); //infantry
        assertNull(cardDeck.getCardSet(1));
    }

    @Test // player owns no territory equal to card
    public void testTradeInSet1() {
        cardDeck.getCard(13).setOwnerID(1); //cavalry
        cardDeck.getCard(6).setOwnerID(1);  //cavalry
        cardDeck.getCard(10).setOwnerID(1); //cavalry
        cardDeck.getCard(41).setOwnerID(1); //cavalry
        cardDeck.getCard(37).setOwnerID(1); //artillery
        cardDeck.getCard(17).setOwnerID(1); //infantry

        Card[] testSet = new Card[3];
        testSet[0] = cardDeck.getCard(13);
        testSet[1] = cardDeck.getCard(6);
        testSet[2] = cardDeck.getCard(10);

        assertEquals(0, cardDeck.getSetsTradedIn());
        assertEquals(4, cardDeck.tradeInSet(testSet, playerTerritory));
        assertEquals(1, cardDeck.getSetsTradedIn());
        assertEquals(-2, cardDeck.getCard(13).getOwnerID());
        assertEquals(-2, cardDeck.getCard(6).getOwnerID());
        assertEquals(-2, cardDeck.getCard(10).getOwnerID());
        assertEquals(1, cardDeck.getCard(41).getOwnerID());
        assertEquals(1, cardDeck.getCard(37).getOwnerID());
        assertEquals(1, cardDeck.getCard(17).getOwnerID());
    }

    @Test // player owns 1 territory -> 2 bonus
    public void testTradeInSet2() {
        cardDeck.getCard(13).setOwnerID(1); //cavalry
        cardDeck.getCard(6).setOwnerID(1);  //cavalry
        cardDeck.getCard(10).setOwnerID(1); //cavalry
        cardDeck.getCard(41).setOwnerID(1); //cavalry
        cardDeck.getCard(37).setOwnerID(1); //artillery
        cardDeck.getCard(17).setOwnerID(1); //infantry

        Card[] testSet = new Card[3];
        testSet[0] = cardDeck.getCard(41);
        testSet[1] = cardDeck.getCard(6);
        testSet[2] = cardDeck.getCard(10);

        assertEquals(0, cardDeck.getSetsTradedIn());
        assertEquals(6, cardDeck.tradeInSet(testSet, playerTerritory));
        assertEquals(1, cardDeck.getSetsTradedIn());
        assertEquals(-2, cardDeck.getCard(41).getOwnerID());
        assertEquals(-2, cardDeck.getCard(6).getOwnerID());
        assertEquals(-2, cardDeck.getCard(10).getOwnerID());
    }

    @Test // player owns 2 territory still only 2 bonus
    public void testTradeInSet3() {
        cardDeck.getCard(13).setOwnerID(1); //cavalry
        cardDeck.getCard(6).setOwnerID(1);  //cavalry
        cardDeck.getCard(10).setOwnerID(1); //cavalry
        cardDeck.getCard(41).setOwnerID(1); //cavalry
        cardDeck.getCard(37).setOwnerID(1); //artillery
        cardDeck.getCard(17).setOwnerID(1); //infantry

        Card[] testSet = new Card[3];
        testSet[0] = cardDeck.getCard(41);
        testSet[1] = cardDeck.getCard(37);
        testSet[2] = cardDeck.getCard(10);

        assertEquals(0, cardDeck.getSetsTradedIn());
        assertEquals(6, cardDeck.tradeInSet(testSet, playerTerritory));
        assertEquals(1, cardDeck.getSetsTradedIn());
        assertEquals(-2, cardDeck.getCard(41).getOwnerID());
        assertEquals(-2, cardDeck.getCard(37).getOwnerID());
        assertEquals(-2, cardDeck.getCard(10).getOwnerID());
    }

    @Test
    public void testTradeInSetAll() {
        int size = cardDeck.getDeck().length;


        // get all cards from deck
        for (int i = 0; i < size; i++) {
            cardDeck.getRandomCard(1);
        }

        Territory[] territories = new Territory[1];
        territories[0] = new Territory(-1);
        Card[] set;

        int i = 0;
        int expectedArmyCount = 2;
        int count = 0;
        while ((set = cardDeck.getCardSet(1)) != null) {
            if (count < 5) {
                expectedArmyCount += 2;
                count++;
            } else if (count == 5) {
                expectedArmyCount = 15;
                count++;
            } else {
                int tmp = count - 5;
                expectedArmyCount = 15 + (5 * tmp);
                count++;
            }
            i++;
            assertEquals(expectedArmyCount, cardDeck.tradeInSet(set, territories));
            assertEquals(i, cardDeck.getSetsTradedIn());
        }
    }


    @Test(expected = IllegalArgumentException.class)
    public void testTradeInSetNoSet1() {
        Card[] set = new Card[5];
        cardDeck.tradeInSet(set, playerTerritory);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testTradeInSetNoSet2() {
        cardDeck.tradeInSet(null, playerTerritory);
    }
}
