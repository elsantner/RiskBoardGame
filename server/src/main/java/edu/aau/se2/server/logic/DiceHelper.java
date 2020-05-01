package edu.aau.se2.server.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.aau.se2.server.data.Player;

public interface DiceHelper {

    /**
     * Generates a random permutation of all Player ids in players list.
     * @param players Players to create permutation for.
     * @return List of random permutation of all Player ids.
     */
    static List<Integer> getRandomTurnOrder(List<Player> players) {
        ArrayList<Integer> startingOrder = new ArrayList<>();
        for (Player p: players) {
            startingOrder.add(p.getUid());
        }
        Collections.shuffle(startingOrder);
        return startingOrder;
    }
}
