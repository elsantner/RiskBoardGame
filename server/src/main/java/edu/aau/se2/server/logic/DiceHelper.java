package edu.aau.se2.server.logic;

import java.util.ArrayList;
import java.util.Collections;

public abstract class DiceHelper {

    /**
     * Generates a random permutation of all i (0 <= i < playerCount).
     * @param playerCount Number of players to create permutation for.
     * @return Random starting order.
     */
    public static ArrayList<Integer> getRandomTurnOrder(int playerCount) {
        ArrayList<Integer> startingOrder = new ArrayList<>();
        for (int i=0; i<playerCount; i++) {
            startingOrder.add(i);
        }
        Collections.shuffle(startingOrder);
        return startingOrder;
    }
}
